document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const loginMessage = document.getElementById("loginMessage");

    if (username === "" || password === "") {
        loginMessage.textContent = "Username and Password cannot be empty.";
        loginMessage.style.color = "red";
        return;
    }

       var xhr = new XMLHttpRequest();
       xhr.open("POST", "http://localhost:8080/login", true);
       xhr.setRequestHeader("Content-Type", "application/json");

       xhr.onreadystatechange = function () {
           if (xhr.readyState === 4) {
               if (xhr.status === 200) {
                   const successData = JSON.parse(xhr.responseText)
                   window.location.href = successData.uri
               } else {
                   console.error("Error:", xhr.status, xhr.responseText);
               }
           }
       };

       xhr.send(JSON.stringify({
           username: username,
           password: password
       }));


});
