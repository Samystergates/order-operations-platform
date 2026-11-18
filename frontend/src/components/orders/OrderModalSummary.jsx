import React from "react";

function formatQuantity(quantity) {
  if (
    (typeof quantity !== "number" && typeof quantity !== "string") ||
    (typeof quantity === "string" && quantity.trim() === "")
  ) {
    return "—";
  }

  const numericQuantity = Number(quantity);
  return Number.isFinite(numericQuantity) ? numericQuantity.toFixed(2) : "—";
}

export default function OrderModalSummary({
  order,
  heading = "OrderData",
  headingStyle,
  containerStyle,
}) {
  return (
    <section>
      <h5 style={{ marginBottom: "20px", ...headingStyle }}>
        {heading}
      </h5>

      <div className="container" style={containerStyle}>
        <div className="row">
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Verkooporder:&nbsp;&nbsp;&nbsp;</strong> {order?.orderNumber}
            </p>
          </div>
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Afnemer:&nbsp;&nbsp;</strong> {order?.user}
            </p>
          </div>
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Naam:&nbsp;&nbsp;&nbsp;</strong> {order?.customerName}
            </p>
          </div>
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Leverdatum:&nbsp;&nbsp;&nbsp;</strong> {order?.deliveryDate}
            </p>
          </div>
        </div>

        <div className="row">
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Artikelnummer:&nbsp;&nbsp;</strong> {order?.product}
            </p>
          </div>
          <div className="col-md-3">
            <p style={{ fontSize: "1rem" }}>
              <strong>Aantal:&nbsp;&nbsp;</strong> {formatQuantity(order?.aantal)}
            </p>
          </div>
          <div className="col-md-6">
            <p style={{ fontSize: "1rem" }}>
              <strong>Omschrijving:&nbsp;&nbsp;</strong> {order?.omsumin}
            </p>
          </div>
        </div>
      </div>

      <hr style={{ margin: "1rem 0" }} />
    </section>
  );
}
