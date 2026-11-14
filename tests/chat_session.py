from playwright.sync_api import sync_playwright
from pathlib import Path
import threading,http.server,functools
root=Path('app/src/main/assets').resolve()
server=http.server.ThreadingHTTPServer(('127.0.0.1',0),functools.partial(http.server.SimpleHTTPRequestHandler,directory=str(root)))
threading.Thread(target=server.serve_forever,daemon=True).start()
with sync_playwright() as p:
 b=p.chromium.launch(args=['--no-sandbox'])
 a,bp=[b.new_context().new_page() for _ in range(2)]
 for page in [a,bp]:page.goto(f'http://localhost:{server.server_port}/chat.html')
 def out(page):page.wait_for_function("document.querySelector('#output').value.length>0");return page.locator('#output').input_value()
 a.click('#host');bp.fill('#input',out(a));bp.click('#apply');a.fill('#input',out(bp));a.click('#apply')
 for page in [a,bp]:page.locator('#confirm').wait_for()
 assert a.locator('#digits').inner_text()==bp.locator('#digits').inner_text()
 a.click('#confirm');assert a.locator('#chat').is_hidden()
 bp.click('#confirm');a.fill('#message','private test');a.click('#send');bp.get_by_text('Open text once').click()
 assert 'private test' in bp.locator('#inbox').inner_text()
 from PIL import Image
 Image.new('RGB',(80,90),'blue').save('/tmp/lumina_test.jpg')
 a.set_input_files('#photo','/tmp/lumina_test.jpg');bp.get_by_text('Open photo once').click()
 bp.wait_for_function("document.querySelector('#inbox img')?.naturalWidth>0")
 bp.wait_for_function("!document.querySelector('#inbox').childElementCount",timeout=16000)
 bp.click('#end');a.wait_for_function("document.querySelector('#chat').hidden")
 print('PASS encrypted WebRTC pairing, verification gate, timed text and photo, session teardown')
