import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGreaterThan, faLessThan } from "@fortawesome/free-solid-svg-icons";

function TransportOrderTable({
  orders,
  isSelected,
  onRowClick,
  onRowDoubleClick,
}) {
  return (
    <table className="table table-hover" style={{ width: "100%", height: "100%" }}>
      <thead>
        <tr>
          <th>Verkooporder</th>
          <th>Naam</th>
          <th>PostCode</th>
          <th>Plaats</th>
          <th>Land</th>
          <th>Leverdatum</th>
        </tr>
      </thead>
      <tbody>
        {orders?.map((item, index) => (
          <tr
            style={{ height: "65px" }}
            key={`${item?.id},${index}`}
            className={isSelected(item) ? "table-active" : ""}
            onClick={() => onRowClick(item)}
            onDoubleClick={() => onRowDoubleClick(item)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                onRowDoubleClick(item);
              }
            }}
            tabIndex="0"
          >
            <td>{item?.orderNumber}</td>
            <td>{item?.user}</td>
            <td>{item?.postCode}</td>
            <td>{item?.city}</td>
            <td>{item?.country}</td>
            <td>{item?.deliveryDate}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function SelectedTransportOrderTable({
  orderMeta,
  tempOrders,
  custOrders,
  draggedIndex,
  setDraggedIndex,
  setOrderMeta,
  isSelected,
  onRowClick,
}) {
  return (
    <table className="table table-hover" style={{ width: "100%", height: "100%" }}>
      <thead>
        <tr>
          <th>Verkooporder</th>
          <th>Naam</th>
          <th>PostCode</th>
          <th>Plaats</th>
          <th>Land</th>
          <th>Leverdatum</th>
        </tr>
      </thead>
      <tbody>
        {orderMeta
          .sort((a, b) => a.orderRowNumber - b.orderRowNumber)
          .map((meta, index) => {
            const order =
              meta.orderType === "MAIN"
                ? tempOrders?.find((o) => o.id === meta.orderId)
                : custOrders?.find((o) => o.id === meta.orderId);

            if (!order) return null;

            return (
              <tr
                key={`${order.id}-${index}`}
                draggable
                onDragStart={() => setDraggedIndex(index)}
                onDragOver={(e) => e.preventDefault()}
                onDrop={() => {
                  if (draggedIndex === null) return;

                  setOrderMeta((prevMeta) => {
                    const sorted = [...prevMeta].sort(
                      (a, b) => a.orderRowNumber - b.orderRowNumber
                    );

                    const [movedItem] = sorted.splice(draggedIndex, 1);
                    sorted.splice(index, 0, movedItem);

                    return sorted.map((item, idx) => ({
                      ...item,
                      orderRowNumber: idx + 1,
                    }));
                  });

                  setDraggedIndex(null);
                }}
                className={isSelected(order) ? "table-active" : ""}
                onClick={() => onRowClick(order)}
              >
                <td>{order.orderNumber}</td>
                <td>{order.customerName}</td>
                <td>{order.postCode}</td>
                <td>{order.city}</td>
                <td>{order.country}</td>
                <td>{order.deliveryDate}</td>
              </tr>
            );
          })}
      </tbody>
    </table>
  );
}

export default function TransportOrderPicker({
  filteredOrder,
  orderMeta,
  tempOrders,
  custOrders,
  draggedIndex,
  setDraggedIndex,
  setOrderMeta,
  colorValue,
  isSelected,
  onRowClick,
  onRowDoubleClick,
  onMoveRight,
  onMoveLeft,
}) {
  const moveDisabled = colorValue !== "N" && colorValue !== "R";

  return (
    <div style={{ display: "flex", flexDirection: "row", height: "73%" }}>
      <div
        style={{
          flex: "0 0 47.5%",
          border: "1px solid black",
          overflowY: "auto",
          overflowX: "auto",
        }}
      >
        <TransportOrderTable
          orders={filteredOrder}
          isSelected={isSelected}
          onRowClick={onRowClick}
          onRowDoubleClick={onRowDoubleClick}
        />
      </div>

      <div
        style={{
          flex: "0 0 5%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <button
          className="btn btn-primary mb-3"
          disabled={moveDisabled}
          style={{ fontSize: "15px", padding: "10px" }}
          onClick={onMoveRight}
        >
          <FontAwesomeIcon icon={faGreaterThan} /> <FontAwesomeIcon icon={faGreaterThan} />
        </button>
        <button
          className="btn btn-secondary"
          disabled={moveDisabled}
          style={{ fontSize: "15px", padding: "10px" }}
          onClick={onMoveLeft}
        >
          <FontAwesomeIcon icon={faLessThan} /> <FontAwesomeIcon icon={faLessThan} />
        </button>
      </div>

      <div
        style={{
          flex: "0 0 47.5%",
          border: "1px solid black",
          overflowY: "auto",
          overflowX: "auto",
        }}
      >
        <SelectedTransportOrderTable
          orderMeta={orderMeta}
          tempOrders={tempOrders}
          custOrders={custOrders}
          draggedIndex={draggedIndex}
          setDraggedIndex={setDraggedIndex}
          setOrderMeta={setOrderMeta}
          isSelected={isSelected}
          onRowClick={onRowClick}
        />
      </div>
    </div>
  );
}
