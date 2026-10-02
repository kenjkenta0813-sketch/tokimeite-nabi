const button = document.getElementById('greet-button');
const message = document.getElementById('message');

button.addEventListener('click', () => {
  message.textContent = 'The app is running!';
});
