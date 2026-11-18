import React from "react";
import { Modal, ModalBody, ModalFooter, ModalHeader } from "reactstrap";

const tableStyle = {
  width: "100%",
  borderCollapse: "collapse",
  border: "1px solid #ccc",
};

const thStyle = {
  padding: "10px",
  border: "1px solid #ccc",
  backgroundColor: "#f2f2f2",
};

const tdStyle = {
  padding: "10px",
  border: "1px solid #ccc",
};

const selectedRowStyle = {
  backgroundColor: "lightblue",
  cursor: "pointer",
};

export default function SavedRoutesModal({
  isOpen,
  toggle,
  routeOrders,
  selectedRouteRowId,
  onSelectRoute,
}) {
  return (
    <Modal
      isOpen={isOpen}
      toggle={toggle}
      fullscreen
      unmountOnClose
      backdrop="static"
      style={{
        maxWidth: "40%",
        maxHeight: "40%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        left: "30%",
        top: "25%",
      }}
      onClick={(e) => e.stopPropagation()}
    >
      <ModalHeader toggle={toggle}>
        <strong>Saved Orders For Routes</strong>
      </ModalHeader>
      <ModalBody>
        <table style={tableStyle}>
          <thead>
            <tr>
              <th style={thStyle}>Entries</th>
            </tr>
          </thead>
          <tbody>
            {routeOrders?.map((item) => (
              <tr
                key={item?.id}
                style={{
                  cursor: "pointer",
                  ...(selectedRouteRowId === item?.id ? selectedRowStyle : {}),
                }}
                onClick={() => onSelectRoute(item?.order?.id)}
                className="hoveredRow"
              >
                <td style={tdStyle}>{item?.id}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </ModalBody>
      <ModalFooter></ModalFooter>
    </Modal>
  );
}
