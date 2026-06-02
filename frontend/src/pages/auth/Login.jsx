export default function Login() {
  return (
    <div className="login-container">
      <h2>Login Page</h2>
      <form>
        <input type="email" placeholder="Email" />
        <input type="password" placeholder="Password" />
        <button type="submit">Login</button>
      </form>
    </div>
  );
}

