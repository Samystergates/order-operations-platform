import React from "react";
import { useCallback, useEffect } from "react";
import { useState } from "react";
import { getCurrentUserDetail } from "../auth";
import { Button, Modal, ModalBody, ModalHeader, ModalFooter } from "reactstrap";
import { loadAllWheelMachineSize } from "../services/machineSizes-service";
import {
  loadSme,
  deleteSme,
  updateSme,
  saveSme,
} from "../services/sme-service";

import { toast } from "react-toastify";
import OrderModalSummary from "./orders/OrderModalSummary";
import SmeFormFields from "./sme/SmeFormFields";
function SmeModal({ smePanelModal, toggleSmeFlowMod, currSme, toggleFlow }) {
  const [user, setUser] = useState(null);

  const [confirmationModal, setConfirmationModal] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loadedEntry, setLoadedEntry] = useState(false);
  const [machineSizes, setMachineSizes] = useState([]);
  const [filteredAndSortedMachineSizes, setFilteredAndSortedMachineSizes] = useState([]);


  const [selectedMerk, setSelectedMerk] = useState("");
  const [selectedRowId, setSelectedRowId] = useState(null);
  const [startOrFinish, setStartOrFinish] = useState(false);
  const [entry, setEntry] = useState(null);
  const [selectedDoorgezet, setSelectedDoorgezet] = useState("");
  const [selectedKoelgaten, setSelectedKoelgaten] = useState("");
  const [selectedTypeBoutgat, setSelectedTypeBoutgat] = useState("");
  const [selectedVerstevigingsringen, setSelectedVerstevigingsringen] =
    useState("");
  const [selectedVentielbeschermer, setSelectedVentielbeschermer] =
    useState("");
  const [selectedWisselsysteem, setSelectedWisselsysteem] = useState("");

  const loadOrderSme = useCallback((entryToLoad) => {
    if (entryToLoad) {
      loadSme(entryToLoad)
        .then((data) => {
          setEntry(data);
          setSelectedDoorgezet(data?.doorgezet);
          setSelectedVerstevigingsringen(data?.verstevigingsringen);
          setSelectedVentielbeschermer(data?.ventielbeschermer);
          setSelectedKoelgaten(data?.koelgaten);
          setSelectedTypeBoutgat(data?.typeBoutgat);
          setSelectedWisselsysteem(data?.aansluitnippel);
          if (data.id !== 0) {
            setStartOrFinish(true);
          } else {
            setStartOrFinish(false);
          }
        })
        .catch((error) => {
          console.log(error);
          setStartOrFinish(false);
        });
    }
  }, []);

  useEffect(() => {
    setUser(getCurrentUserDetail());
    loadMachineSizes();
  }, []);

  useEffect(() => {

  }, [startOrFinish]);

  useEffect(() => {
    if (currSme) {
      const nextEntry = {
        orderNumber: currSme?.orderNumber,
        regel: currSme?.regel,
      };
      setEntry((prevEntry) => ({
        ...prevEntry,
        ...nextEntry,
      }));
      setLoadedEntry((prevLoadedEntry) => !prevLoadedEntry);
      loadOrderSme(nextEntry);
    }
  }, [currSme, loadOrderSme]);

  useEffect(() => {
    if (selectedRowId !== null) {
      const selectedEntry = machineSizes?.find(
        (item) => item.id === selectedRowId
      );
      setEntry({
        ...selectedEntry,
        orderNumber: currSme?.orderNumber,
        regel: currSme?.regel,
      });
      setSelectedDoorgezet(selectedEntry?.doorgezet);
      setSelectedVerstevigingsringen(selectedEntry?.verstevigingsringen);
      setSelectedVentielbeschermer(selectedEntry?.ventielbeschermer);
      setSelectedKoelgaten(selectedEntry?.koelgaten);
      setSelectedTypeBoutgat(selectedEntry?.typeBoutgat);
      setSelectedWisselsysteem(selectedEntry?.wisselsysteem);
    }
  }, [selectedRowId, machineSizes, currSme?.orderNumber, currSme?.regel]);

  window.onerror = function (message, source, lineno, colno, error) {
    console.error("Global error caught:", message, source, lineno, colno, error);
    // Handle the error, such as logging to a service or showing a notification
  };

  function saveOrderSme() {
    // if (!entry?.merk) {
    //   toast.error("merk required");
    //   return;
    // }
    console.log(entry);
    saveSme(entry)
      .then((data) => {
        setEntry(data);
        setSelectedDoorgezet(data?.doorgezet);
        setSelectedVerstevigingsringen(data?.verstevigingsringen);
        setSelectedVentielbeschermer(data?.ventielbeschermer);
        setSelectedKoelgaten(data?.koelgaten);
        setSelectedTypeBoutgat(data?.typeBoutgat);
        setSelectedWisselsysteem(data?.aansluitnippel);
        toast.success("Saved Successfully");
        flowMove("R");

        setStartOrFinish(true);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function updateOrderSme() {
    updateSme(entry)
      .then((data) => {
        setEntry(data);
        setSelectedDoorgezet(data?.doorgezet);
        setSelectedVerstevigingsringen(data?.verstevigingsringen);
        setSelectedVentielbeschermer(data?.ventielbeschermer);
        setSelectedKoelgaten(data?.koelgaten);
        setSelectedTypeBoutgat(data?.typeBoutgat);
        setSelectedWisselsysteem(data?.aansluitnippel);
        toast.success("Saved Successfully");

      })
      .catch((error) => {
        console.log(error);
      });
  }

  function flowMove(colorStatus) {
    const smeIndex = currSme?.departments?.findIndex(
      (entry) => entry.depName === "SME"
    );
    if (smeIndex !== -1) {
      const nextEntries = currSme?.departments?.slice(smeIndex + 2);

      const isValidNext = nextEntries.every(
        (entry) =>
          entry.status === "R" || entry.status === "" || entry.status === "B"
      );

      if (isValidNext) {
        toggleFlow("", currSme?.id, "SME", currSme?.sme, "FWD");
      } else {
        toast.error("Complete Previous");
        console.log("error");
      }
    } else {
      console.log("SME not found in departments");
    }
  }

  const smeRemove = () => {
    if (entry?.id > 0) {
      deleteSme(entry?.id)
        .then((data) => {
          setStartOrFinish(false);
          toggleFlow("", currSme?.id, "SME", currSme?.sme, "RVS");
          clearData();
          toast.success("Removed Successfully");
        })
        .catch((error) => {
          console.log(error);
        });
    }
    toggleSmeFlowMod();
  };


  function loadMachineSizes() {
    loadAllWheelMachineSize()
      .then((data) => {
        setMachineSizes([...data]);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  useEffect(() => {
    if (machineSizes) {
      const tempFilteredAndSortedMachineSizes = machineSizes
        .filter((item) => (selectedMerk ? item.merk === selectedMerk : true))
        .sort((a, b) => {
          const merkA = a.merk || "";
          const merkB = b.merk || "";
          const modelA = a.model || "";
          const modelB = b.model || "";
          const typeA = a.type || "";
          const typeB = b.type || "";

          if (selectedMerk) {
            if (merkA === selectedMerk && merkB !== selectedMerk) return -1;
            if (merkB === selectedMerk && merkA !== selectedMerk) return 1;
          }

          const merkCompare = merkA.localeCompare(merkB, undefined, { numeric: true, sensitivity: "base" });
          if (merkCompare !== 0) return merkCompare;

          const modelCompare = modelA.localeCompare(modelB, undefined, { numeric: true, sensitivity: "base" });
          if (modelCompare !== 0) return modelCompare;

          return typeA.localeCompare(typeB, undefined, { numeric: true, sensitivity: "base" });
        });

      setFilteredAndSortedMachineSizes(tempFilteredAndSortedMachineSizes);

    }
  }, [machineSizes, selectedMerk]);

  const handleMerkChange = (e) => {
    setSelectedMerk(e.target.value);
  };

  const handleChange = (event) => {
    const { id } = event.target;
    const isChecked = event.target.checked;
    const val = isChecked ? "JA" : "NEE";

    switch (id) {
      case "Doorgezet":
        setEntry({ ...entry, doorgezet: val });
        setSelectedDoorgezet(val);
        break;
      case "Koelgaten":
        setEntry({ ...entry, koelgaten: val });
        setSelectedKoelgaten(val);
        break;
      case "Verstevigingsringen":
        setEntry({ ...entry, verstevigingsringen: val });
        setSelectedVerstevigingsringen(val);
        break;
      case "Ventielbeschermer":
        setEntry({ ...entry, ventielbeschermer: val });
        setSelectedVentielbeschermer(val);
        break;
      case "Wisselsysteem":
        setEntry({ ...entry, aansluitnippel: val });
        setSelectedWisselsysteem(val);
        break;
      case "typeBoutgat":
        setEntry({ ...entry, typeBoutgat: event.target.value });
        setSelectedTypeBoutgat(event.target.value)
        break;
      default:
        break;
    }
  };

  const selectModelAndClose = (id) => {
    setSelectedRowId(id);
    toggleModal();
  };

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const handleButtonClick = () => {
    toggleModal();
    loadMachineSizes();
  };

  const handleModalClosed = () => {
    clearData();
  };

  const clearData = () => {
    setSelectedRowId(null);
    setEntry(null);
    setSelectedDoorgezet("");
    setSelectedVerstevigingsringen("");
    setSelectedVentielbeschermer("");
    setSelectedKoelgaten("");
    setSelectedTypeBoutgat("");
    setSelectedWisselsysteem("");
  };

  const toggleConfirmation = (val) => {
    if (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij" || user.departmentsSet[0].depName === "Verkoop"
      || user.departmentsSet[0].depName === "Werkplaats") {
      setConfirmationModal(!confirmationModal);
      if (val === "yes") {
        smeRemove();
      } else if (val === "no") {
      }
    } else {
      toast.error("UnAuthorized Department");
    }
  };

  const distinctMerks = Array.from(
    new Set(machineSizes.map((item) => item.merk))
  ).sort((a, b) => a.localeCompare(b));

  const tableStyle = {
    width: "100%",
    borderCollapse: "collapse",
    border: "1px solid #ccc",
  };

  const thStyle = {
    padding: "10px",
    border: "1px solid #ccc",
    backgroundColor: "#f0f0f0",
  };

  const tdStyle = {
    padding: "10px",
    border: "1px solid #ccc",
  };

  const hoveredRowStyle = {
    backgroundColor: "lightblue",
    cursor: "pointer",
  };

  return (
    <>
      <Modal
        isOpen={smePanelModal}
        toggle={toggleSmeFlowMod}
        onClosed={handleModalClosed}
        fullscreen
        unmountOnClose
        backdrop="static"
        style={{
          maxWidth: "65%",
          maxHeight: "90%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          left: "17%",
          top: "7%",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <ModalHeader toggle={toggleSmeFlowMod}>
          <strong>Toevoegen Smederij Order</strong>
        </ModalHeader>
        <ModalBody>
          <OrderModalSummary
            order={currSme}
            heading={<>{loadedEntry} OrderData {startOrFinish && `- ${entry?.forgeNumber}`}</>}
            headingStyle={{ textDecoration: "underline" }}
            containerStyle={{ marginTop: "16px" }}
          />

          <div
            style={{
              display: "flex",
              flexDirection: "column",
              height: "74%",
              backgroundColor: "#eeeeee",
              overflowX: "auto",
            }}
          >
            <div
              style={{
                display: "flex",
                flexDirection: "row",
                height: "74%",
                backgroundColor: "#eeeeee",
                overflowX: "auto",
              }}
            >
              <SmeFormFields
                entry={entry}
                setEntry={setEntry}
                selectedTypeBoutgat={selectedTypeBoutgat}
                onTypeBoutgatChange={handleChange}
              />

              <div
                style={{ flex: 1, padding: "10px", backgroundColor: "#eeeeee" }}
              >
                <div className="card">
                  <div className="card-header">Machine Data</div>
                  <div className="card-body">
                    <div
                      className="form-group"
                      style={{
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        marginBottom: "5px",
                      }}
                    >
                      <label
                        style={{
                          fontSize: "0.9rem",
                          marginBottom: "5px",
                          marginRight: "10px",
                        }}
                      >
                        Merk:
                      </label>
                      <input
                        type="text"
                        style={{
                          width: "85%",
                          padding: "10px",
                          fontSize: "0.9rem",
                          borderRadius: "3px",
                          border: "1px solid #ccc",
                          height: "40px",
                        }}
                        value={entry?.merk}
                        onChange={(e) =>
                          setEntry({ ...entry, merk: e.target.value })
                        }
                      />
                    </div>
                    <div className="form-group">
                      <label
                        style={{ fontSize: "0.9rem", marginBottom: "5px" }}
                      >
                        Type:
                      </label>
                      <input
                        type="text"
                        style={{
                          width: "41%",
                          padding: "10px",
                          fontSize: "0.9rem",
                          marginBottom: "10px",
                          marginLeft: "12px",
                          borderRadius: "3px",
                          height: "40px",
                          border: "1px solid #ccc",
                        }}
                        value={entry?.model}
                        onChange={(e) =>
                          setEntry({ ...entry, model: e.target.value })
                        }
                      />
                      <input
                        type="text"
                        style={{
                          width: "40%",
                          padding: "10px",
                          fontSize: "0.9rem",
                          borderRadius: "3px",
                          border: "1px solid #ccc",
                          height: "40px",
                          marginLeft: "4%",
                        }}
                        value={entry?.type}
                        onChange={(e) =>
                          setEntry({ ...entry, type: e.target.value })
                        }
                      />
                    </div>
                    <div className="form-group text-center">
                      <button
                        style={{
                          fontSize: "1rem",
                          padding: "10px 20px",
                          backgroundColor: "grey",
                          color: "white",
                          border: "none",
                          borderRadius: "3px",
                        }}
                        onClick={handleButtonClick}
                      >
                        Laad...
                      </button>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: "4%" }}>
                  <div style={{ marginBottom: "10px" }}>
                    <input
                      type="checkbox"
                      id="Doorgezet"
                      checked={selectedDoorgezet === "JA" ? true : false}
                      onChange={handleChange}
                      style={{ marginRight: "10px", transform: "scale(1.3)" }}
                    />
                    <label
                      htmlFor="checkbox1"
                      style={{ fontSize: "1.1rem", marginRight: "20px" }}
                    >
                      Doorgezet
                    </label>
                    <input
                      type="checkbox"
                      id="Koelgaten"
                      checked={selectedKoelgaten === "JA" ? true : false}
                      onChange={handleChange}
                      style={{ marginRight: "10px", transform: "scale(1.3)" }}
                    />
                    <label
                      htmlFor="checkbox2"
                      style={{ fontSize: "1.1rem", marginRight: "20px" }}
                    >
                      Koelgaten
                    </label>
                  </div>
                  <div style={{ marginBottom: "10px" }}>
                    <input
                      type="checkbox"
                      id="Verstevigingsringen"
                      checked={
                        selectedVerstevigingsringen === "JA" ? true : false
                      }
                      onChange={handleChange}
                      style={{ marginRight: "10px", transform: "scale(1.3)" }}
                    />
                    <label
                      htmlFor="checkbox3"
                      style={{ fontSize: "1.1rem", marginRight: "20px" }}
                    >
                      Verstevigingsringen
                    </label>
                    <input
                      type="checkbox"
                      id="Ventielbeschermer"
                      checked={
                        selectedVentielbeschermer === "JA" ? true : false
                      }
                      onChange={handleChange}
                      style={{ marginRight: "10px", transform: "scale(1.3)" }}
                    />
                    <label
                      htmlFor="checkbox4"
                      style={{ fontSize: "1.1rem", marginRight: "20px" }}
                    >
                      Ventielbeschermer
                    </label>
                    <select
                      style={{
                        width: "30%",
                        border: "1px solid #ccc",
                        outline: "none",
                        background: "white",
                        padding: "5px",
                        borderRadius: "3px",
                      }}
                      value={entry?.optionVentielbeschermer}
                      onChange={(e) =>
                        setEntry({ ...entry, optionVentielbeschermer: e.target.value })
                      }
                    >
                      <option value=""></option>
                      <option value="BEUGEL">BEUGEL</option>
                      <option value="GASDOP">GASDOP</option>
                      <option value="HALVE MAAN">HALVE MAAN</option>
                    </select>
                  </div>
                  <div>
                    <input
                      type="checkbox"
                      id="Wisselsysteem"
                      checked={selectedWisselsysteem === "JA" ? true : false}
                      onChange={handleChange}
                      style={{ marginRight: "10px", transform: "scale(1.3)" }}
                    />
                    <label
                      htmlFor="checkbox5"
                      style={{ fontSize: "1.1rem", marginRight: "20px" }}
                    >
                      Aansluitnippel druk/wisselsysteem
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <div
              style={{
                display: "flex",
                flexDirection: "row",
                height: "26%",
                backgroundColor: "#eeeeee",
              }}
            >
              <div style={{ flex: 1, padding: "10px" }}>
                <div>
                  <label style={{ display: "block", marginBottom: "5px" }}>
                    <strong>Opmerking:</strong>
                  </label>
                  <textarea
                    rows="3"
                    cols="50"
                    value={entry?.opmerking}
                    onChange={(e) =>
                      setEntry({ ...entry, opmerking: e.target.value })
                    }
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
        </ModalBody>
        <ModalFooter>
          {!startOrFinish && (
            <Button color="primary" onClick={() => saveOrderSme()}>
              Opslaan
            </Button>
          )}

          {startOrFinish && currSme?.sme === "R" && (
            <Button color="warning" onClick={() => updateOrderSme()}>
              Update
            </Button>
          )}

          {startOrFinish && currSme?.sme === "R" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
            <div style={{ display: "flex", justifyContent: "flex-start" }}>
              <Button
                color="success"
                onClick={() => flowMove("Y")}
                style={{ marginRight: "400px" }}
              >
                Start
              </Button>
            </div>
          )}
          {startOrFinish && currSme?.sme === "Y" && (
            <Button color="dark" onClick={() => flowMove("G")}>
              Afmelden
            </Button>
          )}

          {currSme?.sme !== "Y" && currSme?.sme !== "G" && (
            <Button color="secondary" onClick={() => toggleConfirmation()}>
              Verwijder
            </Button>
          )}
        </ModalFooter>
      </Modal>
      <Modal
        isOpen={isModalOpen}
        toggle={toggleModal}
        fullscreen
        unmountOnClose
        backdrop="static"
        style={{
          maxWidth: "60%",
          maxHeight: "60%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          left: "20%",
          top: "20%",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <ModalHeader toggle={toggleModal}>
          <strong>Select Model</strong>
        </ModalHeader>
        <ModalBody>
          <div className="col-md-4" style={{ width: "50%" }}>
            <div>
              <label htmlFor="searchField"></label>
              <select
                style={{
                  width: "50%",
                  border: "3px solid #ccc",
                  outline: "none",
                  background: "white",
                  padding: "5px",
                  borderRadius: "3px",
                }}
                value={selectedMerk}
                onChange={handleMerkChange}
              >
                <option value="">Selecteer merk</option>
                {distinctMerks.map((merk) => (
                  <option key={merk} value={merk}>
                    {merk}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div style={{ padding: "5px" }}></div>
          <table style={tableStyle}>
            <thead>
              <tr>
                <th style={thStyle}>ID</th>
                <th style={thStyle}>Merk</th>
                <th style={thStyle}>Model</th>
                <th style={thStyle}>Type</th>
                <th style={thStyle}>Bandenmaat</th>
                <th style={thStyle}>Velgmaat</th>
                <th style={thStyle}>Velgtype</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedMachineSizes?.map((item, index) => (
                <tr
                  key={item.id}
                  style={{
                    ...(selectedRowId === item.id ? hoveredRowStyle : {}),
                  }}
                  onClick={() => selectModelAndClose(item.id)}
                  className="hoveredRow"
                >
                  <td style={tdStyle}>{index + 1}</td>
                  <td style={tdStyle}>{item.merk}</td>
                  <td style={tdStyle}>{item.model}</td>
                  <td style={tdStyle}>{item.type}</td>
                  <td style={tdStyle}>{item.bandenmaat}</td>
                  <td style={tdStyle}>{item.velgmaat}</td>
                  <td style={tdStyle}>{item.velgtype}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </ModalBody>
        <ModalFooter></ModalFooter>
      </Modal>
      <Modal isOpen={confirmationModal} unmountOnClose
        backdrop="static" toggle={toggleConfirmation}>
        <ModalBody style={{ justifyContent: "center" }}>
          Remove SME data for Order {entry?.orderNumber}
        </ModalBody>
        <ModalFooter style={{ justifyContent: "center" }}>
          <Button
            color="primary"
            value="yes"
            onClick={(event) => toggleConfirmation(event.target.value)}
          >
            Yes
          </Button>{" "}
          <Button
            color="secondary"
            value="no"
            onClick={(event) => toggleConfirmation(event.target.value)}
          >
            No
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
}

export default SmeModal;
