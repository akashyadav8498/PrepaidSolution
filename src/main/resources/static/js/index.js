// Role selector activation logic
// document.querySelectorAll('.role-option').forEach(option => {
//     option.addEventListener('click', () => {
//         document.querySelectorAll('.role-option').forEach(opt => opt.classList.remove('active'));
//         option.classList.add('active');
//     });
// });

// Form submission handler
document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const loginMessage = document.getElementById("loginMessage");

    // Basic field validation
    if (username === "" || password === "") {
        loginMessage.textContent = "Username and Password cannot be empty.";
        loginMessage.style.color = "red";
        return;
    }

    // const role = document.querySelector('.role-option.active').getAttribute('data-role').toUpperCase();

    try {
        const response = await fetch("http://localhost:8080/login", {
       // const response = await fetch("http://192.168.1.10:8080/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                username: username,
                password: password,
                // role: role
            }),
            redirect: "follow", // browser will follow if it's a real redirect
        });

        // If server returns JSON with redirect URL
        if (response.ok) {
            const contentType = response.headers.get("content-type");

            if (contentType && contentType.includes("application/json")) {
                const data = await response.json();
                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                } else {
                    loginMessage.textContent = "Login succeeded but no redirection path returned.";
                    loginMessage.style.color = "green";
                }
            } else {
                // If it’s not JSON (e.g., HTML after redirect), assume server handled it
                window.location.href = response.url;
            }
        } else {
            const text = await response.text();
            loginMessage.textContent = "Login failed: " + text;
            loginMessage.style.color = "red";
        }
    } catch (error) {
        console.error("Login error:", error);
        loginMessage.textContent = "Server error. Please try again later.";
        loginMessage.style.color = "red";
    }
});
