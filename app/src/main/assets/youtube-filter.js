(() => {
  if (!/(^|\.)youtube\.com$/i.test(location.hostname) || window.__luminaAdFilter) return;
  window.__luminaAdFilter = true;
  const style = document.createElement('style');
  style.textContent = `
    ytd-ad-slot-renderer, ytm-ad-slot-renderer, ytm-promoted-sparkles-web-renderer,
    ytd-promoted-sparkles-web-renderer, ytd-display-ad-renderer,
    .ytp-ad-overlay-container { display:none!important }
    .html5-video-player.ad-showing video {visibility:hidden!important}
  `;
  (document.head || document.documentElement).appendChild(style);
  const held = new Map();
  function release(video) {
    const prior = held.get(video);
    if (video.muted) video.muted = prior.muted;
    if (video.playbackRate === 16) video.playbackRate = prior.rate;
    held.delete(video);
  }
  function check() {
    const active = document.querySelector('.html5-video-player.ad-showing');
    const video = active && active.querySelector('video');
    for (const old of held.keys()) if (old !== video) release(old);
    if (!active) return;
    if (video) {
      if (!held.has(video)) held.set(video,{muted:video.muted,rate:video.playbackRate});
      video.muted = true;
    }
    const skip = [...active.querySelectorAll('button.ytp-skip-ad-button,button.ytp-ad-skip-button,button.ytp-ad-skip-button-modern,.ytp-ad-skip-button-container button')]
      .find(button => !button.disabled && button.getClientRects().length && getComputedStyle(button).visibility !== 'hidden');
    if (skip) { skip.click(); return; }
    // Only advance a separately identified ad clip. Stitched streams may also contain the main video.
    if (video && active.querySelector('.ytp-ad-text.ytp-ad-preview-text,.ytp-ad-simple-ad-badge,.ytp-ad-player-overlay') &&
        Number.isFinite(video.duration) && video.duration > 0 && video.duration <= 120) {
      try { video.playbackRate = 16; } catch (_) {}
    }
  }
  const observer = new MutationObserver(check);
  observer.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['class','disabled']});
  document.addEventListener('playing',check,true);
  setInterval(check,250);
  check();
})();
