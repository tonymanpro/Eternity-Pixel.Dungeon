/**
 * main.js — Eternity Pixel Dungeon Website
 * General UI logic: navbar scroll effect, mobile menu, lightbox, toasts.
 */
(function() {
  'use strict';

  /* ── Navbar scroll shadow ── */
  const navbar = document.querySelector('.navbar');
  if (navbar) {
    window.addEventListener('scroll', () => {
      navbar.classList.toggle('scrolled', window.scrollY > 40);
    }, { passive: true });
  }

  /* ── Mobile nav toggle ── */
  const navToggle = document.querySelector('.nav-toggle');
  const navLinks  = document.querySelector('.navbar-links');
  if (navToggle && navLinks) {
    navToggle.addEventListener('click', () => {
      navLinks.classList.toggle('open');
      const isOpen = navLinks.classList.contains('open');
      navToggle.setAttribute('aria-expanded', isOpen);
    });
    // Close on nav link click
    navLinks.querySelectorAll('a').forEach(a => {
      a.addEventListener('click', () => navLinks.classList.remove('open'));
    });
  }

  /* ── Mark active nav link ── */
  function setActiveNav() {
    const path = window.location.pathname;
    document.querySelectorAll('.navbar-links a').forEach(a => {
      const href = a.getAttribute('href');
      const isActive =
        (href === '/' || href === '/index.html') && (path === '/' || path === '/index.html') ||
        (href !== '/' && href !== '/index.html' && path.startsWith(href.replace('.html', '')));
      a.classList.toggle('active', isActive);
    });
  }
  setActiveNav();

  /* ── Lightbox ── */
  const lightbox = document.getElementById('lightbox');
  if (lightbox) {
    const lbImg = lightbox.querySelector('img');
    document.querySelectorAll('.gallery-item img').forEach(img => {
      img.parentElement.addEventListener('click', () => {
        lbImg.src = img.src;
        lightbox.classList.add('open');
        document.body.style.overflow = 'hidden';
      });
    });
    lightbox.addEventListener('click', e => {
      if (e.target === lightbox || e.target.classList.contains('lightbox-close')) {
        lightbox.classList.remove('open');
        document.body.style.overflow = '';
      }
    });
    document.addEventListener('keydown', e => {
      if (e.key === 'Escape') {
        lightbox.classList.remove('open');
        document.body.style.overflow = '';
      }
    });
  }

  /* ── Toast notifications ── */
  window.showToast = function(msg, type) {
    let toast = document.getElementById('toast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'toast';
      toast.className = 'toast';
      document.body.appendChild(toast);
    }
    toast.textContent = msg;
    toast.className = 'toast' + (type === 'error' ? ' error' : '');
    void toast.offsetWidth; // reflow
    toast.classList.add('show');
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => toast.classList.remove('show'), 3500);
  };

  /* ── Intersection Observer fade-in ── */
  const fadeEls = document.querySelectorAll('.fade-in-scroll');
  if ('IntersectionObserver' in window && fadeEls.length) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1 });
    fadeEls.forEach(el => io.observe(el));
  }

  /* ── Smooth scroll for anchor links ── */
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', e => {
      const target = document.querySelector(a.getAttribute('href'));
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });

})();
