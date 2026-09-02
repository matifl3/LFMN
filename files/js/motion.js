/* ============================================================
   LFM Nacional — Motion: reveal al scroll, stagger, count-up
   Auto-inicializable: escanea el DOM y observa nodos dinámicos.
   ============================================================ */
(function () {
  'use strict';

  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  var COUNT_DURATION = 900;
  var STAGGER_SELECTOR = '.race-row, .feed-item, tbody tr, .entrant-row, .file-row, .comment, .vote-row';
  var REVEAL_AUTO = '.hero-content, .section-head, .empty-state, .detail-header, .profile-header, .info-grid > *, .stat-grid > *, .admin-layout > *';

  var io = ('IntersectionObserver' in window)
    ? new IntersectionObserver(handleIntersect, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' })
    : null;

  /* ---------- Functional helpers ---------- */

  function parseNumber(el) {
    var txt = (el.getAttribute('data-count') || el.textContent || '').replace(/\s/g, '').trim();
    if (!/^-?\d+(\.\d+)?$/.test(txt)) return NaN;
    return parseFloat(txt);
  }

  function runCountUp(el) {
    var target = parseNumber(el);
    if (isNaN(target)) return;
    el.classList.add('counted');
    var dec = (String(target).split('.')[1] || '').length;
    var suffix = el.getAttribute('data-suffix') || '';
    var prefix = el.getAttribute('data-prefix') || '';
    var start = performance.now();
    function tick(now) {
      var p = Math.min(1, (now - start) / COUNT_DURATION);
      var eased = 1 - Math.pow(1 - p, 3);
      el.textContent = prefix + (target * eased).toFixed(dec) + suffix;
      if (p < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  function handleIntersect(entries) {
    entries.forEach(function (entry) {
      if (!entry.isIntersecting) return;
      var el = entry.target;
      if (el.classList.contains('reveal')) el.classList.add('is-in');
      if (el.classList.contains('count-up')) runCountUp(el);
      io.unobserve(el);
    });
  }

  function observe(el) {
    if (io) io.observe(el);
    else {
      if (el.classList.contains('reveal')) el.classList.add('is-in');
      if (el.classList.contains('count-up')) runCountUp(el);
    }
  }

  function staggerList(container) {
    if (!container || !container.children) return;
    var items = container.children;
    for (var i = 0; i < items.length; i++) {
      var it = items[i];
      if (it.nodeType !== 1) continue;
      if (!container.classList.contains('motion-list') && !it.matches(STAGGER_SELECTOR)) continue;
      if (it.classList.contains('motion-item')) continue;
      it.style.setProperty('--i', i);
      it.classList.add('motion-item');
    }
  }

  function autoEnhance(root) {
    var i, el;
    var reveals = root.querySelectorAll ? root.querySelectorAll('.reveal:not(.is-in)') : [];
    for (i = 0; i < reveals.length; i++) observe(reveals[i]);

    var counts = root.querySelectorAll ? root.querySelectorAll('.count-up:not(.counted)') : [];
    for (i = 0; i < counts.length; i++) observe(counts[i]);

    if (root.nodeType === 1) {
      if (root.matches('.reveal:not(.is-in)')) observe(root);
      if (root.matches('.count-up:not(.counted)')) observe(root);
    }

    /* Reveal automático sobre bloques estructurales (una vez) */
    if (root.nodeType === 1 && root.matches(REVEAL_AUTO) && !root.classList.contains('reveal')) {
      /* solo si está dentro del flujo principal y no dentro de un modal */
      if (!root.closest('.modal-overlay')) {
        root.classList.add('reveal', 'reveal-up');
        observe(root);
      }
    }
    var auto = root.querySelectorAll ? root.querySelectorAll(REVEAL_AUTO + ':not(.reveal)') : [];
    for (i = 0; i < auto.length; i++) {
      if (!auto[i].closest('.modal-overlay')) {
        auto[i].classList.add('reveal', 'reveal-up');
        observe(auto[i]);
      }
    }

    /* Count-up automático sobre valores numéricos puros */
    if (root.nodeType === 1 && root.matches && root.matches('.stat-value, .rating-value, .hero-stat .stat-value')
        && !root.classList.contains('count-up') && !isNaN(parseNumber(root))) {
      root.classList.add('count-up');
      observe(root);
    }
    var statVals = root.querySelectorAll ? root.querySelectorAll('.stat-value:not(.count-up), .rating-value:not(.count-up), .hero-stat .stat-value:not(.count-up)') : [];
    for (i = 0; i < statVals.length; i++) {
      if (!isNaN(parseNumber(statVals[i]))) {
        statVals[i].classList.add('count-up');
        observe(statVals[i]);
      }
    }

    /* Stagger de listas detectadas dinámicamente */
    if (root.nodeType === 1) {
      if (root.matches('[data-motion-list], .motion-list')) staggerList(root);
      else if (root.matches(STAGGER_SELECTOR)) {
        var parent = root.parentElement;
        if (parent) staggerList(parent);
      }
    }
    var lists = root.querySelectorAll ? root.querySelectorAll('[data-motion-list], .motion-list') : [];
    for (i = 0; i < lists.length; i++) staggerList(lists[i]);
  }

  function scan(root) {
    autoEnhance(root);
  }

  /* ---------- Observador global de renders dinámicos ---------- */

  var mo = ('MutationObserver' in window)
    ? new MutationObserver(function (mutations) {
        for (var i = 0; i < mutations.length; i++) {
          var added = mutations[i].addedNodes;
          for (var j = 0; j < added.length; j++) {
            if (added[j].nodeType === 1) scan(added[j]);
          }
        }
      })
    : null;

  function init() {
    scan(document);
    if (mo) mo.observe(document.body, { childList: true, subtree: true });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  /* ---------- API pública ---------- */

  window.LFM = window.LFM || {};
  window.LFM.motion = {
    init: init,
    reveal: function (el) { el.classList.add('reveal'); observe(el); },
    stagger: staggerList,
    countUp: runCountUp
  };
})();