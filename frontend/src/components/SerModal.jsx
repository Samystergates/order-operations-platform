import React from "react";
import { useCallback, useEffect } from "react";
import { useState } from "react";
import { getCurrentUserDetail } from "../auth";
import { Button, Modal, ModalBody, ModalHeader, ModalFooter } from "reactstrap";
import { loadAllMonteur, loadSer, saveSer, updateSer, deleteSer } from "../services/ser-services";
import ComboInput from "./ComboInput";
import { toast } from "react-toastify";
function SerModal({ serPanelModal, toggleSerMod, currSer, toggleFlow }) {
    const [user, setUser] = useState(null);
    const [monteur1, setMonteur1] = useState("");
    const [monteur2, setMonteur2] = useState("");

    const [monteurs, setMonteurs] = useState([]);

    const [confirmationModal, setConfirmationModal] = useState(false);
    const [startOrFinish, setStartOrFinish] = useState(false);
    const [entry, setEntry] = useState(null);


    useEffect(() => {
        setUser(getCurrentUserDetail());
        loadMonteurs();
    }, []);

    useEffect(() => {
    }, [startOrFinish]);

    const loadOrderSer = useCallback((entryToLoad) => {
        if (entryToLoad) {
            loadSer(entryToLoad)
                .then((data) => {
                    setMonteur1(data.monteur1);
                    setMonteur2(data.monteur2);
                    setEntry(data);
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
        if (currSer) {
            const nextEntry = {
                orderNumber: currSer?.orderNumber,
            };
            setEntry((prevEntry) => ({
                ...prevEntry,
                ...nextEntry,
            }));
            loadOrderSer(nextEntry);
        }
    }, [currSer, loadOrderSer]);

    window.onerror = function (message, source, lineno, colno, error) {
        console.error("Global error caught:", message, source, lineno, colno, error);
    };

    function saveOrderSer() {
        saveSer(entry)
            .then((data) => {
                setEntry(data);
                toast.success("Saved Successfully");
                setStartOrFinish(true);
            })
            .catch((error) => {
                console.log(error);
            });
    }

    function updateOrderSer() {
        updateSer(entry)
            .then((data) => {
                setEntry(data);
                toast.success("Saved Successfully");
            })
            .catch((error) => {
                console.log(error);
            });
    }

    function flowMove(colorStatus) {
        const smeIndex = currSer?.departments?.findIndex(
            (entry) => entry.depName === "SER"
        );
        if (smeIndex !== -1) {
            const nextEntries = currSer?.departments?.slice(smeIndex + 2);

            const isValidNext = nextEntries.every(
                (entry) =>
                    entry.status === "R" || entry.status === "" || entry.status === "B"
            );

            if (isValidNext) {
                toggleFlow("", currSer?.id, "SER", currSer?.ser, "FWD");
            } else {
                toast.error("Complete Previous");
            }
        } else {
            toast.error("SER department not found");
        }
    }

    const serRemove = () => {
        if (entry?.id > 0) {
            deleteSer(entry?.id)
                .then((data) => {
                    setStartOrFinish(false);
                    // toggleFlow("", currSer?.id, "SME", currSer?.sme, "RVS");
                    clearData();
                    toast.success("Removed Successfully");
                })
                .catch((error) => {
                    console.log(error);
                });
        }
        toggleSerMod();
    };


    function loadMonteurs() {
        loadAllMonteur()
            .then((data) => {
                setMonteurs([...data]);
            })
            .catch((error) => {
                console.log(error);
            });
    }

    const handleChangeMonteur1 = (monteurText) => {
        setEntry({ ...entry, monteur1: monteurText });
        setMonteur1(monteurText);
    };

    const handleChangeMonteur2 = (monteurText) => {
        setEntry({ ...entry, monteur2: monteurText });
        setMonteur2(monteurText);
    };

    const handleModalClosed = () => {
        clearData();
    };

    const getOptionsWithout = (excludeValue) => {
        return monteurs.filter(m => m !== excludeValue);
    };

    const clearData = () => {
        setMonteur1("");
        setMonteur2("");
        setEntry(null);
    };

    const toggleConfirmation = (val) => {
        if (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij" || user.departmentsSet[0].depName === "Verkoop"
            || user.departmentsSet[0].depName === "Werkplaats") {
            setConfirmationModal(!confirmationModal);
            if (val === "yes") {
                serRemove();
            } else if (val === "no") {
            }
        } else {
            toast.error("UnAuthorized Department");
        }
    };

    return (
        <>
            <Modal
                isOpen={serPanelModal}
                toggle={toggleSerMod}
                onClosed={handleModalClosed}
                fullscreen
                unmountOnClose
                backdrop="static"
                style={{
                    maxWidth: "30%",
                    maxHeight: "35%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    left: "35%",
                    top: "15%",
                    overflow: "visible"
                }}
                onClick={(e) => e.stopPropagation()}
            >
                <ModalHeader toggle={toggleSerMod}>
                    <strong>Starten Order</strong>
                </ModalHeader>
                <ModalBody>
                    <div
                        style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "flex-start",
                            paddingTop: 5,
                            gap: 10,
                        }}
                    >
                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                            <label style={{ minWidth: 120, textAlign: "left" }}>Verkooporder:</label>
                            <input
                                type="text"
                                value={currSer?.orderNumber}
                                disabled
                                style={{
                                    width: 200,
                                    padding: 8,
                                    border: "1px solid #ccc",
                                    borderRadius: 4,
                                    background: "#f5f5f5",
                                }}
                            />
                        </div>

                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                            <label style={{ minWidth: 120, textAlign: "left" }}>Monteur 1:</label>
                            <ComboInput
                                value={monteur1}
                                onChange={handleChangeMonteur1}
                                // options={monteurs}
                                options={getOptionsWithout(monteur2)}
                                placeholder=""
                            />
                        </div>

                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                            <label style={{ minWidth: 120, textAlign: "left" }}>Monteur 2:</label>
                            <ComboInput
                                value={monteur2}
                                onChange={handleChangeMonteur2}
                                // options={monteurs}
                                options={getOptionsWithout(monteur1)}
                                placeholder=""
                            />
                        </div>
                    </div>
                </ModalBody>
                <ModalFooter
                    style={{
                        display: "flex",
                        justifyContent: "flex-end",
                        gap: "10px",
                    }}
                >
                    {!startOrFinish && currSer?.ser === "R" && entry?.id === 0 && (
                        <Button color="primary" onClick={saveOrderSer}>
                            Opslaan
                        </Button>
                    )}

                    {startOrFinish && currSer?.ser === "R" && entry?.id !== 0 && (
                        <Button color="warning" onClick={updateOrderSer}>
                            Update
                        </Button>
                    )}

                    {startOrFinish && currSer?.ser === "R" && (
                        (user.departmentsSet[0].depName === "ADMIN" ||
                            user.departmentsSet[0].depName === "Logistiek") && (
                            <Button color="success" onClick={() => flowMove("Y")}>
                                Start
                            </Button>
                        )
                    )}

                    {startOrFinish && currSer?.ser === "Y" && (
                        <Button color="dark" onClick={() => flowMove("G")}>
                            Afmelden
                        </Button>
                    )}

                    {currSer?.ser !== "Y" && currSer?.ser !== "G" && (
                        <Button color="secondary" onClick={toggleConfirmation}>
                            Verwijder
                        </Button>
                    )}
                </ModalFooter>
            </Modal>
            <Modal isOpen={confirmationModal} unmountOnClose
                backdrop="static" toggle={toggleConfirmation}>
                <ModalBody style={{ justifyContent: "center" }}>
                    Remove SER data for Order {entry?.orderNumber}
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

export default SerModal;
