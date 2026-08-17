/**
 * blog.js — Eternity Pixel Dungeon Website
 * Loads posts from Firestore and renders them.
 * Depends on: firebase.js, i18n.js
 */
(function() {
  'use strict';

  // Format a Firestore Timestamp or ISO date string
  function formatDate(raw) {
    try {
      const d = (raw && raw.toDate) ? raw.toDate() : new Date(raw);
      return d.toLocaleDateString(i18n.lang === 'es' ? 'es-MX' : 'en-US', {
        year: 'numeric', month: 'long', day: 'numeric'
      });
    } catch { return ''; }
  }

  // Render a badge for each tag
  function renderTags(tags) {
    if (!tags || !tags.length) return '';
    return tags.map(tag => `<span class="badge badge-feature">${tag}</span>`).join('');
  }

  // Build a post card HTML
  function buildPostCard(post, lang) {
    const title   = post[`title_${lang}`]   || post.title_en   || '';
    const summary = post[`summary_${lang}`] || post.summary_en || '';
    const date    = formatDate(post.date);
    const img     = post.image || 'assets/images/screenshots/gameplay.jpg';
    const tags    = post.tags || [];
    return `
      <article class="post-card fade-in-scroll">
        <img class="post-card-img" src="${img}" alt="${title}" loading="lazy">
        <div class="post-card-body">
          <div class="post-card-tags">${renderTags(tags)}</div>
          <h2 class="post-card-title">
            <a href="blog-post.html?id=${post.id}">${title}</a>
          </h2>
          <div class="post-card-meta">
            <span>📅 ${date}</span>
          </div>
          <p class="post-card-excerpt">${summary}</p>
          <div class="post-card-footer">
            <a href="blog-post.html?id=${post.id}" class="btn btn-outline" data-i18n="blog.read_more"></a>
          </div>
        </div>
      </article>
    `;
  }

  // Render the latest posts into a container
  async function renderLatestPosts(containerId, maxResults) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const lang = (window.i18n && window.i18n.lang) || 'en';
    container.innerHTML = `<p class="text-muted text-center" data-i18n="blog.loading">${i18n.t('blog.loading')}</p>`;

    const posts = await EPD.getLatestPosts(maxResults || 3);
    if (!posts.length) {
      container.innerHTML = `<p class="text-muted text-center" data-i18n="blog.no_posts">${i18n.t('blog.no_posts')}</p>`;
      return;
    }
    container.innerHTML = posts.map(p => buildPostCard(p, lang)).join('');
    // Re-apply i18n to newly created elements
    i18n.applyAll(container);
    // Re-trigger scroll animations
    container.querySelectorAll('.fade-in-scroll').forEach(el => {
      const io = new IntersectionObserver(entries => {
        entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('visible'); io.unobserve(e.target); } });
      }, { threshold: 0.1 });
      io.observe(el);
    });
  }

  // Render all posts into a container (blog.html)
  async function renderAllPosts(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const lang = (window.i18n && window.i18n.lang) || 'en';
    container.innerHTML = `<p class="text-muted text-center" data-i18n="blog.loading">${i18n.t('blog.loading')}</p>`;

    const posts = await EPD.getAllPosts();
    if (!posts.length) {
      container.innerHTML = `<p class="text-muted text-center" data-i18n="blog.no_posts">${i18n.t('blog.no_posts')}</p>`;
      return;
    }
    container.innerHTML = `<div class="posts-grid">${posts.map(p => buildPostCard(p, lang)).join('')}</div>`;
    i18n.applyAll(container);
  }

  // Render a single post (blog-post.html)
  async function renderSinglePost() {
    const params = new URLSearchParams(window.location.search);
    const id     = params.get('id');
    const lang   = (window.i18n && window.i18n.lang) || 'en';
    const contentEl = document.getElementById('post-content');
    if (!contentEl || !id) return;

    const post = await EPD.getPost(id);
    if (!post) {
      contentEl.innerHTML = '<p class="text-muted text-center">Post not found.</p>';
      return;
    }

    const title   = post[`title_${lang}`]   || post.title_en || '';
    const body    = post[`body_${lang}`]     || post.body_en  || '';
    const date    = formatDate(post.date);
    const img     = post.image || 'assets/images/screenshots/gameplay.jpg';

    document.title = `${title} — Eternity Pixel Dungeon`;

    contentEl.innerHTML = `
      <div class="post-header">
        <div class="post-header-tags">${renderTags(post.tags || [])}</div>
        <h1 class="post-title">${title}</h1>
        <div class="post-meta">📅 ${date}</div>
      </div>
      <img class="post-hero-img" src="${img}" alt="${title}">
      <div class="post-body markdown-content" id="post-body-md"></div>
    `;

    // Render Markdown (using marked.js loaded in HTML)
    if (window.marked) {
      document.getElementById('post-body-md').innerHTML = marked.parse(body);
    } else {
      document.getElementById('post-body-md').textContent = body;
    }
  }

  // Re-render on language change
  document.addEventListener('langchanged', () => {
    if (document.getElementById('latest-posts'))   renderLatestPosts('latest-posts', 3);
    if (document.getElementById('all-posts'))       renderAllPosts('all-posts');
    if (document.getElementById('post-content'))    renderSinglePost();
  });

  // Export
  window.EPD = window.EPD || {};
  Object.assign(window.EPD, { renderLatestPosts, renderAllPosts, renderSinglePost });
})();
