import React, { useEffect, useState } from "react";
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from "reactstrap";
import { toast } from "react-toastify";
import { createCustomOrder, deleteCustomOrderApi } from "../services/order-service";

function CustomOrderFormModal({
  isOpen,
  toggle,
  selectedOrder,
  isEditing,
  onSave,
  onDelete
}) {
  const [order, setOrder] = useState({
    orderNumber: "",
    customerName: "",
    postCode: "",
    city: "",
    country: "",
    deliveryDate: "",
    opmerking: "",
  });

  useEffect(() => {
    if (isEditing && selectedOrder) {
      setOrder(selectedOrder);
    } else {
      resetForm();
    }
  }, [isEditing, selectedOrder]);

  const handleToggle = () => {
    toggle();
    resetForm();
  }

  const resetForm = () => {
    setOrder({
      orderNumber: "",
      customerName: "",
      postCode: "",
      city: "",
      country: "",
      deliveryDate: "",
      opmerking: "",
    });
  };

  const handleSave = () => {
    if (!order.orderNumber || !order.customerName) {
      toast.error("Ordernummer en klantnaam zijn verplicht.");
      return;
    }
    saveCustomOrder(order);
    toast.success(`Order ${isEditing ? "bijgewerkt" : "toegevoegd"}!`);
    // toggle();
    // resetForm();
  };

  function saveCustomOrder(entry) {
    createCustomOrder(entry)
      .then((data) => {
        setOrder(data);
        onSave(data);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  const handleDelete = () => {
    if (!order.orderNumber || !order.customerName) {
      toast.error("Ordernummer en klantnaam zijn verplicht.");
      return;
    }
    deleteCustomOrder(order);
    toast.success("Order verwijderd!");
  };


  function deleteCustomOrder(entry) {
    deleteCustomOrderApi(entry)
      .then((data) => {
        onDelete(order.id);
        resetForm();
      })
      .catch((error) => {
        console.log(error);
      });
  }

  return (
    // <Draggable handle=".modal-header">

    <Modal
      isOpen={isOpen}
      toggle={handleToggle}
      onClosed={resetForm}
      unmountOnClose
      backdrop="static"
      style={{
        maxWidth: "50%",
        top: "20%",
      }}
    >
      <ModalHeader toggle={toggle} className="modal-header">
        <strong>{isEditing ? "Handmatige Orderregel Bewerken" : "Handmatige Orderregel Toevoegen"}</strong>
      </ModalHeader>
      <ModalBody>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "20px" }}>
          <div style={{ flex: "1 1 45%" }}>
            <label><strong>Ordernummer</strong></label>
            <input
              type="text"
              className="form-control"
              value={order.orderNumber}
              onChange={(e) => setOrder({ ...order, orderNumber: e.target.value })}
            />

            <label className="mt-3"><strong>Naam</strong></label>
            <input
              type="text"
              className="form-control"
              value={order.customerName}
              onChange={(e) => setOrder({ ...order, customerName: e.target.value })}
            />

            <label className="mt-3"><strong>Postcode</strong></label>
            <input
              type="text"
              className="form-control"
              value={order.postCode}
              onChange={(e) => setOrder({ ...order, postCode: e.target.value })}
            />
          </div>

          <div style={{ flex: "1 1 45%" }}>
            <label><strong>Plaats</strong></label>
            <input
              type="text"
              className="form-control"
              value={order.city}
              onChange={(e) => setOrder({ ...order, city: e.target.value })}
            />

            <label className="mt-3"><strong>Land</strong></label>
            <input
              type="text"
              className="form-control"
              value={order.country}
              onChange={(e) => setOrder({ ...order, country: e.target.value })}
            />

            <label className="mt-3"><strong>Leverdatum</strong></label>
            <input
              type="date"
              className="form-control"
              value={order.deliveryDate}
              onChange={(e) => setOrder({ ...order, deliveryDate: e.target.value })}
            />
          </div>
        </div>

        <div className="mt-4">
          <label><strong>Opmerking</strong></label>
          <textarea
            rows={3}
            className="form-control"
            value={order.opmerking}
            onChange={(e) => setOrder({ ...order, opmerking: e.target.value })}
          />
        </div>
      </ModalBody>
      <ModalFooter>
        <Button color="primary" onClick={handleSave}>
          {isEditing ? "Bijwerken" : "Opslaan"}
        </Button>
        {isEditing && <Button
          color="danger"
          onClick={handleDelete}
        >
          Verwijder
        </Button>}
      </ModalFooter>
    </Modal>
    // </Draggable>
  );
}

export default CustomOrderFormModal;
