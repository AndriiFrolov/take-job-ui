export function expandCollapse(id) {
  const element = document.getElementById(id);

  if (!element.className.includes("d-block")) {
    element.className = element.className.replace("d-none", "d-block");
  } else {
    element.className = element.className.replace("d-block", "d-none");
  }
}

export function caret(expandId) {
  return $('<span class="caret py-0">')
    .click(function () {
      expandCollapse(expandId);
      toggleCaret(this);
    })
}

export function toggleCaret(element) {
  element.classList.toggle("caret-down");
}

export function getJiraFilterLink(filter, value) {
  return $('<a>').attr('href', 'https://one-jira.pearson.com/browse/PMCCP-114926?jql=' + encodeURIComponent(filter)).attr('target', '_blank').text(value);
}

export function getJiraLink(ticketId, text) {
  return $('<a class="py-0 link-primary">').attr('href', 'https://one-jira.pearson.com/browse/' + ticketId).attr('target', '_blank').text(text);
}

export function getBitbucketLink(codeRepository, path, summary, line) {
  return $('<a>').attr('href', 'https://bitbucket.pearson.com/projects/PMC/repos/' + codeRepository + '/browse/' + path + '#' + line).attr('target', '_blank').text(summary);
}
