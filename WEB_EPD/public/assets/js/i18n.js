/**
 * i18n.js — Eternity Pixel Dungeon Website
 * Lightweight internationalization engine.
 * - Detects browser language or reads from localStorage
 * - Fetches translations/{lang}.json (relative path, works locally and on Firebase)
 * - Applies strings to all [data-i18n] elements
 * - Exposes window.i18n.t(key) for JS-side translations
 */

(function() {
  'use strict';

  const SUPPORTED_LANGS = ['en', 'es'];
  const DEFAULT_LANG    = 'en';
  const STORAGE_KEY     = 'epd_lang';

  let _strings = {};
  let _lang    = DEFAULT_LANG;

  /**
   * Build the correct path to a translation file.
   * Handles three scenarios:
   *   1. file://  → relative path computed from depth inside public/
   *   2. npx serve . (WEB_EPD as root) → URL contains /public/; prefix accordingly
   *   3. firebase serve / deployed      → root IS public/; use absolute /translations/
   */
  function translationUrl(lang) {
    const path = window.location.pathname.replace(/\\/g, '/');

    if (window.location.protocol === 'file:') {
      // Compute relative path based on depth inside "public/"
      const parts = path.split('/');
      const publicIdx = parts.findIndex(p => p.toLowerCase() === 'public');
      const depth = publicIdx >= 0 ? Math.max(0, parts.length - publicIdx - 2) : 0;
      return '../'.repeat(depth) + `translations/${lang}.json`;
    }

    // HTTP/HTTPS: check if /public/ appears in the URL path
    // e.g. http://localhost:3000/public/index.html → serve root is WEB_EPD/
    const segments = path.split('/');
    const publicIdx = segments.findIndex(s => s.toLowerCase() === 'public');
    if (publicIdx > 0) {
      // Build prefix up to and including "public"
      const base = segments.slice(0, publicIdx + 1).join('/');
      return `${base}/translations/${lang}.json`;
    }

    // Standard: firebase serve or deployed to Firebase Hosting
    return `/translations/${lang}.json`;
  }

  /** Detect best language */
  function detectLang() {
    // 1. localStorage preference
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored && SUPPORTED_LANGS.includes(stored)) return stored;
    // 2. Browser language
    const nav = (navigator.language || navigator.userLanguage || 'en').slice(0, 2).toLowerCase();
    return SUPPORTED_LANGS.includes(nav) ? nav : DEFAULT_LANG;
  }

  /** Fetch and apply a language */
  async function loadLang(lang) {
    _lang = lang;
    localStorage.setItem(STORAGE_KEY, lang);
    try {
      const res = await fetch(translationUrl(lang));
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      _strings = await res.json();
    } catch (e) {
      // Fallback: try English
      if (lang !== DEFAULT_LANG) {
        console.warn(`i18n: failed to load '${lang}', falling back to '${DEFAULT_LANG}'`);
        try {
          const res = await fetch(translationUrl(DEFAULT_LANG));
          _strings = await res.json();
        } catch (e2) {
          console.error('i18n: could not load any translations', e2);
          _strings = {};
        }
      } else {
        console.error('i18n: could not load any translations', e);
        _strings = {};
      }
    }
    applyAll();
    updateToggleUI();
    document.documentElement.lang = lang;
    document.dispatchEvent(new CustomEvent('langchanged', { detail: { lang } }));
  }

  /** Translate a key, with optional template vars: t('x', {name: 'Y'}) */
  function t(key, vars) {
    let val = _strings[key] || key;
    if (vars) {
      Object.keys(vars).forEach(k => {
        val = val.replace(new RegExp(`{${k}}`, 'g'), vars[k]);
      });
    }
    return val;
  }

  /** Apply translations to all [data-i18n] elements in the DOM */
  function applyAll(root) {
    const ctx = root || document;
    ctx.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.getAttribute('data-i18n');
      const attr = el.getAttribute('data-i18n-attr'); // e.g. "placeholder"
      const val = t(key);
      if (attr) {
        el.setAttribute(attr, val);
      } else if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
        el.placeholder = val;
      } else {
        // Support \n for multi-line (e.g. admin title)
        el.innerHTML = val.replace(/\n/g, '<br>');
      }
    });
  }

  /** Update the language toggle buttons in the UI */
  function updateToggleUI() {
    document.querySelectorAll('.lang-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.lang === _lang);
    });
  }

  /** Switch language on user click */
  function switchLang(lang) {
    if (lang === _lang) return;
    loadLang(lang);
  }

  /** Wire up toggle buttons once DOM is ready */
  function bindToggles() {
    document.querySelectorAll('.lang-btn').forEach(btn => {
      btn.addEventListener('click', () => switchLang(btn.dataset.lang));
    });
  }

  /** Init */
  async function init() {
    bindToggles();
    await loadLang(detectLang());
  }

  // Public API
  window.i18n = { t, applyAll, switchLang, get lang() { return _lang; } };

  // Auto-init on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
