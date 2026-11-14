from pathlib import Path
from playwright.sync_api import sync_playwright
script=Path('app/src/main/assets/youtube-filter.js').read_text()
with sync_playwright() as p:
    browser=p.chromium.launch()
    page=browser.new_page()
    page.route('https://m.youtube.com/**',lambda route:route.fulfill(content_type='text/html',body='<div class="html5-video-player"><video></video><button class="ytp-skip-ad-button" hidden>Skip</button></div>'))
    page.goto('https://m.youtube.com/watch?v=fixture')
    page.evaluate(script)
    page.evaluate("""() => {
        window.player=document.querySelector('div');
        window.video=document.querySelector('video');
        window.skip=document.querySelector('button');
        window.skips=0;
        skip.onclick=()=>{window.skips++;player.classList.remove('ad-showing')};
        player.classList.add('ad-showing');
    }""")
    page.wait_for_function("video.muted && getComputedStyle(video).visibility==='hidden'")
    assert page.evaluate("skips===0")
    page.evaluate("skip.hidden=false")
    page.wait_for_function("skips===1 && !video.muted && getComputedStyle(video).visibility==='visible'")
    page.evaluate("video.muted=true; player.classList.add('ad-showing')")
    page.wait_for_function("skips===2 && video.muted")
    print('PASS: hidden skip ignored, ad muted and hidden, enabled skip clicked, content restored, original mute preserved.')
