import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import { getCurrentUser } from "../services/authService";
import { getPayments } from "../services/paymentService";

function Payments() {
  const [payments, setPayments] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function load() {
      const user = getCurrentUser();
      if (!user) {
        setMessage("Login to see payments.");
        return;
      }

      try {
        const res = await getPayments(user.id);
        setPayments(res.data);
      } catch {
        setMessage("Could not load payments.");
      }
    }

    load();
  }, []);

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Payments</h1>
        <p className="section-subtitle">View payment history and receipts.</p>

        {message && <div className="info-box">{message}</div>}

        <div className="table-card card">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Amount</th>
                <th>Method</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id}>
                  <td>#{p.id}</td>
                  <td>{p.amount} RON</td>
                  <td>{p.method}</td>
                  <td>{p.paymentDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}

export default Payments;