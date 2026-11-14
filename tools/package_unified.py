"""Retain compiled original resources and DEX; extend the binary manifest without renumbering resource IDs."""
import struct as st
import sys
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED, ZIP_STORED
def u(data,p): return st.unpack_from('<I',data,p)[0]
def h(data,p): return st.unpack_from('<H',data,p)[0]
def put(data,p,value): st.pack_into('<I',data,p,value)
def update(data):
    p=8; chunks=[]; names=[]; inserted=[]; in_home=False
    while p<len(data):
        tag,header,size=st.unpack_from('<HHI',data,p); chunk=bytearray(data[p:p+size]);p+=size
        if tag==1:
            assert h(chunk,2)==28 and u(chunk,12)==0 and u(chunk,16)==0
            for i in range(u(chunk,8)):
                j=u(chunk,20)+u(chunk,28+4*i);names.append(chunk[j+2:j+2+2*h(chunk,j)].decode('utf-16le'))
            chunks.append(None);pool_index=len(chunks)-1;continue
        if tag in (258,259):
            kind=names[u(chunk,20)]
            if tag==258:
                attrs={}
                for i in range(h(chunk,28)):
                    a=16+h(chunk,24)+i*h(chunk,26);attrs[names[u(chunk,a+4)]]=a
                def val(key):return names[u(chunk,attrs[key]+16)]
                def assign(key,value):
                    if value not in names:names.append(value)
                    a=attrs[key];put(chunk,a+8,names.index(value));put(chunk,a+16,names.index(value))
                if kind=='manifest':
                    assign('package','com.lumina.focus.unified');assign('versionName','2.2');put(chunk,attrs['versionCode']+16,22)
                if kind=='application':assign('label','Lumina Focus One')
                if kind in ('activity','service','receiver') and val('name').startswith('.'):assign('name','com.lumina.focus'+val('name'))
                if kind=='activity' and val('name')=='com.lumina.focus.Home':
                    template=bytearray(chunk);put(chunk,attrs['exported']+16,0);in_home=True
                if in_home and kind=='intent-filter':
                    filterparts=[chunk];chunks.append(b'');continue
            if in_home and 'filterparts' in locals():
                filterparts.append(chunk)
                if tag==259 and kind=='intent-filter':
                    launcher=b''.join(filterparts);del filterparts
                continue
            if tag==259 and kind=='activity' and in_home:
                in_home=False
                for cls in ('Hub','PrivateChat','FocusBrowser'):
                    new=bytearray(template);name='com.luminastudios.callguard.'+cls;names.append(name)
                    for i in range(h(new,28)):
                        a=16+h(new,24)+i*h(new,26);k=names[u(new,a+4)]
                        if k=='name':put(new,a+8,len(names)-1);put(new,a+16,len(names)-1)
                        if k=='exported':put(new,a+16,0xffffffff if cls=='Hub' else 0)
                    inserted.extend([new,launcher if cls=='Hub' else b'',chunk])
            if tag==259 and kind=='application':chunks.extend(inserted)
        chunks.append(chunk)
    offsets=[];raw=bytearray()
    for name in names:
        v=name.encode('utf-16le');offsets.append(len(raw));raw.extend(st.pack('<H',len(v)//2)+v+b'\0\0')
    raw.extend(b'\0'*(-len(raw)%4));start=28+4*len(names)
    chunks[pool_index]=st.pack('<HHIIIIII',1,28,start+len(raw),len(names),0,0,start,0)+st.pack('<%dI'%len(names),*offsets)+raw
    body=b''.join(chunks);return st.pack('<HHI',3,8,len(body)+8)+body
with ZipFile(sys.argv[1]) as original,ZipFile(sys.argv[2]) as preview,ZipFile(sys.argv[3],'w') as out:
    for i in original.infolist():
        if i.filename.startswith('META-INF/'): continue
        val=original.read(i)
        if i.filename=='AndroidManifest.xml':val=update(val)
        out.writestr(i,val)
    for idx,n in enumerate(sorted(n for n in preview.namelist() if n.startswith('classes') and n.endswith('.dex')),2):
        out.writestr('classes%d.dex'%idx,preview.read(n),ZIP_DEFLATED)
    for asset in ('assets/chat.html','assets/youtube-filter.js'):
        out.writestr(asset,preview.read(asset),ZIP_DEFLATED)
