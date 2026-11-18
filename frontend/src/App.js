import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import { useContext, useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Signup from "./pages/user-routes/Signup";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import Userdashboard from "./pages/user-routes/Userdashboard";
import Archived from "./pages/user-routes/Archived";
import Privateroute from "./components/Privateroute";
import UserProvider from "./context/UserProvider";
import { isLoggedIn } from './auth';
import PublicReservedRoute from "./components/PublicReservedRoute";
import userContext from "./context/userContext";

const AuthExpiredHandler = () => {
  const navigate = useNavigate();
  const userContextData = useContext(userContext);

  useEffect(() => {
    const handleAuthExpired = () => {
      userContextData?.setUser({
        data: undefined,
        login: false,
      });
      navigate("/app/login", { replace: true });
    };

    window.addEventListener("auth:expired", handleAuthExpired);
    return () => window.removeEventListener("auth:expired", handleAuthExpired);
  }, [navigate, userContextData]);

  return null;
};

const AppRoutes = () => {
  const userContextData = useContext(userContext);
  const canRegisterUsers = userContextData?.user?.data?.depId === 1;

  return (
    <>
      <AuthExpiredHandler />
      <ToastContainer position="bottom-center" />
      <Routes>
        <Route
          path="/"
          element={<Navigate to={isLoggedIn() ? "/user/dashboard" : "/app/login"} />}
        />

        <Route path="/user" element={<Privateroute />}>
          <Route path="dashboard" element={<Userdashboard />} />
          <Route path="archived" element={<Archived />} />
          {canRegisterUsers && <Route path="signup" element={<Signup />} />}
        </Route>

        <Route path="/app" element={<PublicReservedRoute />}>
          <Route path="login" element={<Login />} />
        </Route>
      </Routes>
    </>
  );
};

function App() {
  return (
    <UserProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </UserProvider>
  );
}

export default App;
