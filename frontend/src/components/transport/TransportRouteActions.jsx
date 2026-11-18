import React from "react";
import { Button, ModalFooter } from "reactstrap";

export default function TransportRouteActions({
  canManageTransport,
  unloadedEntry,
  colorValue,
  onCustomOrder,
  onSave,
  onUpdate,
  onDelete,
  onExisting,
  onStartRoute,
  onFinishRoute,
}) {
  const routeStartedOrFinished = colorValue === "G" || colorValue === "Y";
  const routeNotReadyToFinish = colorValue === "G" || colorValue === "R";

  return (
    <ModalFooter>
      <div style={{ marginRight: "14%" }}>
        {canManageTransport && (
          <Button
            style={{
              marginRight: "30px",
              border: "2px solid #333",
              backgroundColor: "#f8f9fa",
              color: "#000",
            }}
            color="light"
            onClick={onCustomOrder}
          >
            Handmatige regel
          </Button>
        )}
      </div>

      <div style={{ marginRight: "5%" }}>
        {unloadedEntry && canManageTransport && (
          <Button style={{ marginRight: "30px" }} color="primary" onClick={onSave}>
            Opslaan
          </Button>
        )}

        {!unloadedEntry && canManageTransport && (
          <Button
            style={{ marginRight: "30px" }}
            color="primary"
            onClick={onUpdate}
            disabled={routeStartedOrFinished}
          >
            Wijzigen
          </Button>
        )}

        {canManageTransport && (
          <Button
            color="danger"
            onClick={onDelete}
            disabled={unloadedEntry || routeStartedOrFinished}
          >
            Verwijder
          </Button>
        )}
      </div>

      <div style={{ marginRight: "8%" }}>
        <Button color="dark" onClick={onExisting}>
          Existing
        </Button>
      </div>

      <div style={{ marginRight: "8%" }}>
        {canManageTransport && (
          <Button
            color="success"
            style={{ marginRight: "30px" }}
            onClick={onStartRoute}
            disabled={unloadedEntry || routeStartedOrFinished}
          >
            Start Route
          </Button>
        )}
        {canManageTransport && (
          <Button
            color="secondary"
            onClick={onFinishRoute}
            disabled={unloadedEntry || routeNotReadyToFinish}
          >
            Afmelden route
          </Button>
        )}
      </div>
    </ModalFooter>
  );
}
