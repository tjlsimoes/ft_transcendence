function checkStatus() {
  fetch('/')
    .then(response => {
      if (!response.ok) {
        throw new Error('Not ready');
      }
      return response.text();
    })
    .then(html => {
      if (!html.includes('id="loading-page-identifier"')) {
        window.location.reload();
      } else {
        setTimeout(checkStatus, 2000);
      }
    })
    .catch(() => {
      setTimeout(checkStatus, 2000);
    });
}

setTimeout(checkStatus, 1000);
