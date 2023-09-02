window.onload = function () {
  fetch("/navbar.html")
    .then(response => {
      return response.text()
    })
    .then(data => {
      document.querySelector('#header').insertAdjacentHTML('afterbegin', data);
      addPagesToNavbar();
    });
}

const generatePagesTabs = () => {
  const pages = [
    {
      id: "main-page",
      href: "/",
      name: "Search"
    },
    {
      id: "stats-page",
      href: "/stats.html",
      name: "Stats"
    },
    {
      id: "logs-page",
      href: "/logs.html",
      name: "Logs"
    }
  ];

  return pages.map(page => {
    return window.location.pathname === page.href
      ? `<li class="nav-item"><a id=${page.id} class="nav-link active" href=${page.href}>${page.name}</a></li>`
      : `<li class="nav-item"><a id=${page.id} class="nav-link" href=${page.href}>${page.name}</a></li>`;
  }).join('');
}

const addPagesToNavbar = () => {
  const pagesTabs = generatePagesTabs();

  document.querySelector('.navbar-nav').insertAdjacentHTML('afterbegin', pagesTabs);
}