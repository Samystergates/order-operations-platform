import React from "react";
import {
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
} from "reactstrap";

export default function OrderActionModals({
  backorderModal,
  confirmationModal,
  noteModal,
  backorderOrderNumber,
  note,
  onNoteChange,
  onToggleBackorder,
  onToggleConfirmation,
  onToggleNote,
  onSaveNote,
}) {
  return (
    <>
      <Modal
        isOpen={backorderModal}
        unmountOnClose
        backdrop="static"
        toggle={onToggleBackorder}
      >
        <ModalBody style={{ justifyContent: "center" }}>
          Pas back-order status aan voor: {backorderOrderNumber}
        </ModalBody>
        <ModalFooter style={{ justifyContent: "center" }}>
          <Button color="primary" value="yes" onClick={(event) => onToggleBackorder(event.target.value)}>
            Yes
          </Button>{" "}
          <Button color="secondary" value="no" onClick={(event) => onToggleBackorder(event.target.value)}>
            No
          </Button>
        </ModalFooter>
      </Modal>

      <Modal
        isOpen={confirmationModal}
        unmountOnClose
        backdrop="static"
        toggle={onToggleConfirmation}
      >
        <ModalBody style={{ justifyContent: "center" }}>
          Start CRM Orders Fetching Process?
        </ModalBody>
        <ModalFooter style={{ justifyContent: "center" }}>
          <Button color="primary" value="yes" onClick={(event) => onToggleConfirmation(event.target.value)}>
            Yes
          </Button>{" "}
          <Button color="secondary" value="no" onClick={(event) => onToggleConfirmation(event.target.value)}>
            No
          </Button>
        </ModalFooter>
      </Modal>

      <Modal
        isOpen={noteModal}
        unmountOnClose
        backdrop="static"
        toggle={onToggleNote}
      >
        <ModalHeader toggle={onToggleNote}>Add Note</ModalHeader>
        <ModalBody style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
          <textarea
            value={note}
            onChange={(event) => onNoteChange(event.target.value)}
            placeholder="Enter your note here"
            rows="3"
            cols="60"
          />
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={onSaveNote}>Save</Button>{" "}
          <Button color="secondary" onClick={onToggleNote}>Cancel</Button>
        </ModalFooter>
      </Modal>
    </>
  );
}
