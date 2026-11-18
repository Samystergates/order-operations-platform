import React from "react";
import { useEffect } from "react";
import { useState } from "react";
import { getCurrentUserDetail } from "../auth";
import { Button, Modal, ModalBody, ModalHeader, ModalFooter } from "reactstrap";
import {
  loadSpu,
  deleteSpu,
  updateSpu,
  saveSpu,
  loadAllPriceCodes,
  loadAllSpuDepartments,
} from "../services/spu-service";
import { toast } from "react-toastify";
import carWheelImage from "../image/car-wheel.png";
import carWheelImageWhite from "../image/car-wheel-white.png";
import { loadAllWheelColors } from "../services/wheelColor-service";
import useLatestCallback from "../hooks/useLatestCallback";
import {
  findSpuPriceCode,
  shouldUseWhiteWheelImage,
} from "./spu/spuModalLogic";
import SpuCoatingFields from "./spu/SpuCoatingFields";
import OrderModalSummary from "./orders/OrderModalSummary";
function SpuModal({ spuPanelModal, toggleSpuFlowMod, currSpu, toggleFlow }) {
  const [selectedRadio, setSelectedRadio] = useState(null);
  const [checkedCombinations, setCheckedCombinations] = useState([]);
  const [confirmationModal, setConfirmationModal] = useState(false);
  const [user, setUser] = useState(null);
  const [startOrFinish, setStartOrFinish] = useState(false);
  const [wheelColor, setWheelColor] = useState([]);
  const [priceCodes, setPriceCodes] = useState([]);
  const [spuDepartments, setSpuDepartments] = useState([]);
  const [manualPrijscode, setManualPrijscode] = useState("");
  const [manualAfdeling, setManualAfdeling] = useState("");

  const [colorEntry, setColorEntry] = useState(null);
  const [selectedAanValue, setSelectedAanValue] = useState(null);
  const [aanOptions] = useState(["Gevak B.V.", "Van Kappel", "Van den Hurk"]);

  const [, setCheckedNatLakkenn] = useState("NEE");
  const [, setCheckedPoedercoaten] = useState("NEE");
  const [checkedStralen, setCheckedStralen] = useState("NEE");
  const [checkedStralenGedeeltelijk, setCheckedStralenGedeeltelijk] =
    useState("NEE");
  const [checkedSchooperen, setCheckedSchooperen] = useState("NEE");
  const [checkedKitten, setCheckedKitten] = useState("NEE");
  const [checkedPrimer, setCheckedPrimer] = useState("NEE");
  const [checkedOntlakken, setCheckedOntlakken] = useState("NEE");
  const [checkedAflakken, setCheckedAflakken] = useState("NEE");
  const [checkedBlankelak, setCheckedBlankelak] = useState("NEE");
  const [loadedEntry, setLoadedEntry] = useState(false);
  const [finalEntry, setFinalEntry] = useState(null);
  const loadOrderSpuRef = useLatestCallback(loadOrderSpu);


  useEffect(() => {
    setUser(getCurrentUserDetail());
    loadWheelColor();
  }, []);

  useEffect(() => {
    if (currSpu) {
      setFinalEntry((prevFinalEntry) => ({
        ...prevFinalEntry,
        orderNumber: currSpu?.orderNumber,
        regel: currSpu?.regel,
      }));
      setLoadedEntry((prevLoadedEntry) => !prevLoadedEntry);
    }
  }, [currSpu]);

  useEffect(() => {
    loadPriceCodes();
    loadSpuDepartments();
  }, [loadedEntry]);

  useEffect(() => {
    loadOrderSpuRef.current();
  }, [priceCodes, loadOrderSpuRef]);

  useEffect(() => {
    if (!currSpu || !selectedRadio || !priceCodes.length) return;
    if (extractWheelInfo(currSpu.omsumin)) return;

    const matchedCode = findSpuPriceCode(
      priceCodes,
      selectedRadio,
      checkedCombinations,
      currSpu?.zoeknaam
    );

    if (matchedCode !== null) {
      setFinalEntry((prev) => ({ ...prev, prijscode: matchedCode }));
    }
  }, [selectedRadio, currSpu, checkedCombinations, priceCodes]);


  function loadWheelColor() {
    loadAllWheelColors()
      .then((data) => {
        setWheelColor([...data]);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function loadPriceCodes() {
    loadAllPriceCodes()
      .then((data) => {
        setPriceCodes([...data]);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function loadSpuDepartments() {
    loadAllSpuDepartments()
      .then((data) => {
        setSpuDepartments([...data]);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function loadOrderSpu() {
    if (finalEntry) {
      loadSpu(finalEntry)
        .then((data) => {
          if (data.id === 0) {

            const extractedInfo = extractWheelInfo(currSpu.omsumin);
            if (extractedInfo) {
              const matchedEntry = priceCodes
                .filter(entry => entry.spuOrderType === "DESCRIPTION")
                .find(entry => entry.velgmaat === extractedInfo);
              if (matchedEntry) {
                const code = matchedEntry.code;
                setManualPrijscode(code);
                setFinalEntry(prev => ({ ...prev, prijscode: code }));
              }
            }

            const extractedDepCode = extractSpuDepCode(currSpu.cdProdGrp);
            if (extractedDepCode) {
              const matchedEntry = spuDepartments.find(entry => entry.departmentCode === extractedDepCode)
              if (matchedEntry) {
                const name = matchedEntry.departmentName;
                setManualAfdeling(name);
                setFinalEntry(prev => ({ ...prev, afdeling: name }));
              }
            }

            setStartOrFinish(false);
          } else {
            setFinalEntry(data);
            setCheckedStralen(data.stralen);
            setCheckedStralenGedeeltelijk(data.stralenGedeeltelijk);
            setCheckedSchooperen(data.schooperen);
            setCheckedKitten(data.kitten);
            setCheckedPrimer(data.primer);
            setCheckedOntlakken(data.ontlakken);
            setCheckedAflakken(data.aflakken);
            setCheckedBlankelak(data.blankeLak);
            setCheckedNatLakkenn(data.natLakken);
            setCheckedPoedercoaten(data.poedercoaten);
            const selectedOps = [];

            if (data.stralen === "JA") selectedOps.push("Stralen");
            if (data.aflakken === "JA") selectedOps.push("Aflakken");
            if (data.primer === "JA") selectedOps.push("Primer");
            if (data.schooperen === "JA") selectedOps.push("Schooperen");


            setCheckedCombinations(selectedOps);
            setSelectedRadio(
              data.natLakken === "JA" && data.poedercoaten === "NEE"
                ? "natLakken"
                : data.natLakken === "NEE" && data.poedercoaten === "JA"
                  ? "poedercoaten"
                  : ""
            );
            setSelectedAanValue(data.aan || null);
            const ralCodeString = data.ralCode;
            const ralCodeValues = ralCodeString
              .split(",")
              .map((value) => parseInt(value.trim(), 10));


            const foundColor = wheelColor.find(
              (color) =>
                color.colorName === data.kleurOmschrijving &&
                color.red === ralCodeValues[0] &&
                color.green === ralCodeValues[1] &&
                color.blue === ralCodeValues[2]
            );
            const matchedId = foundColor ? foundColor.id : null;
            setManualAfdeling(data.afdeling);
            setManualPrijscode(data.prijscode);
            setColorEntry((prevEntry) => ({
              ...prevEntry,
              id: matchedId,
              red: ralCodeValues[0] || 0,
              green: ralCodeValues[1] || 0,
              blue: ralCodeValues[2] || 0,
              colorName: data.kleurOmschrijving,
            }));
            setStartOrFinish(true);
          }
        })
        .catch((error) => {
          console.log("Error in loadSpu:", error);
          setStartOrFinish(false);
        });
    }
  }

  function saveOrderSpu() {
    if (!finalEntry?.kleurOmschrijving) {
      toast.error("color name required");
      return;
    }

    const spuIndex = currSpu?.departments?.findIndex(
      (entry) => entry.depName === "SPU"
    );
    if (spuIndex !== -1) {
      const previousEntries = currSpu?.departments?.slice(0, spuIndex);

      const isValidPrevious = previousEntries.every(
        (entry) =>
          entry.status === "" || entry.status === "G" || ((entry.status === "R" || entry.status === "Y") && currSpu?.spu === ""));

      if (!isValidPrevious) {
        toast.error("complete previous");
        return;
      }
    }

    saveSpu(finalEntry)
      .then((data) => {
        setFinalEntry(data);
        setCheckedStralen(data.stralen);
        setCheckedStralenGedeeltelijk(data.stralenGedeeltelijk);
        setCheckedSchooperen(data.schooperen);
        setCheckedKitten(data.kitten);
        setCheckedPrimer(data.primer);
        setCheckedOntlakken(data.ontlakken);
        setCheckedAflakken(data.aflakken);
        setCheckedBlankelak(data.blankeLak);
        setCheckedNatLakkenn(data.natLakken);
        setCheckedPoedercoaten(data.poedercoaten);
        setManualAfdeling(data.afdeling);
        const selectedOps = [];

        if (data.stralen === "JA") selectedOps.push("Stralen");
        if (data.aflakken === "JA") selectedOps.push("Aflakken");
        if (data.primer === "JA") selectedOps.push("Primer");
        if (data.schooperen === "JA") selectedOps.push("Schooperen");

        setCheckedCombinations(selectedOps);
        setSelectedRadio(
          data.natLakken === "JA" && data.poedercoaten === "NEE"
            ? "natLakken"
            : data.natLakken === "NEE" && data.poedercoaten === "JA"
              ? "poedercoaten"
              : ""
        );
        setManualPrijscode(data.prijscode);

        flowMove("R");
        setStartOrFinish(true);
        toast.success("Saved Successfully");

      })
      .catch((error) => {
        console.log(error);
      });
  }


  function updateOrderSpu() {
    if (!finalEntry?.kleurOmschrijving) {
      toast.error("color name required");
      return;
    }
    updateSpu(finalEntry)
      .then((data) => {
        setFinalEntry(data);
        setCheckedStralen(data.stralen);
        setCheckedStralenGedeeltelijk(data.stralenGedeeltelijk);
        setCheckedSchooperen(data.schooperen);
        setCheckedKitten(data.kitten);
        setCheckedPrimer(data.primer);
        setCheckedOntlakken(data.ontlakken);
        setCheckedAflakken(data.aflakken);
        setCheckedBlankelak(data.blankeLak);
        setCheckedNatLakkenn(data.natLakken);
        setCheckedPoedercoaten(data.poedercoaten);
        setManualAfdeling(data.afdeling);
        const selectedOps = [];

        if (data.stralen === "JA") selectedOps.push("Stralen");
        if (data.aflakken === "JA") selectedOps.push("Aflakken");
        if (data.primer === "JA") selectedOps.push("Primer");
        if (data.schooperen === "JA") selectedOps.push("Schooperen");

        setCheckedCombinations(selectedOps);
        setSelectedRadio(
          data.natLakken === "JA" && data.poedercoaten === "NEE"
            ? "natLakken"
            : data.natLakken === "NEE" && data.poedercoaten === "JA"
              ? "poedercoaten"
              : ""
        );
        setManualPrijscode(data.prijscode);

        toast.success("Saved Successfully");

      })
      .catch((error) => {
        console.log(error);
      });
  }

  function extractSpuDepCode(departmentCode) {
    if (!departmentCode) return null;

    return departmentCode.substring(3, 6);
  }

  function extractWheelInfo(description) {
    if (!description) return null;

    const text = description.trim().toLowerCase();

    // Normalize spaces and lowercase
    const normalized = text.replace(/\s+/g, ' ').toLowerCase();

    // Match exact allowed size patterns
    const allowedSizes = [
      "36.0 x 25",
      "36.0 x 32",
      "27 x 32",
      "27 x 42"
    ];

    for (const size of allowedSizes) {
      // Create a regex to allow flexible spacing and optional ".0"
      const [w1, , w2] = size.split(" ");
      const pattern = new RegExp(
        `\\b${w1.replace('.0', '(\\.0)?')}\\s*x\\s*${w2.replace('.0', '(\\.0)?')}\\b`,
        "i"
      );
      const match = description.match(pattern);
      if (match) {
        // Return normalized match as e.g., "36 x 25" (with spaces, no .0)
        return `${parseInt(w1)} x ${parseInt(w2)}`;
      }
    }

    // Match exact text phrases
    if (normalized.startsWith("loaderwiel 3 en 5-delig")) {
      return "Loaderwiel 3 en 5-delig";
    }

    if (normalized.startsWith("sportwiel tm 20")) {
      return 'Sportwiel tm 20" + blanke lak';
    }

    return null;
  }



  function flowMove(colorStatus) {
    const spuIndex = currSpu?.departments?.findIndex(
      (entry) => entry.depName === "SPU"
    );
    if (spuIndex !== -1) {
      const previousEntries = currSpu?.departments?.slice(0, spuIndex);
      const nextEntries = currSpu?.departments?.slice(spuIndex + 1);

      const isValidPrevious = previousEntries.every(
        (entry) =>
          entry.status === "G" ||
          entry.status === "" ||
          ((entry.status === "R" || entry.status === "Y") && entry.depName === "SME" && currSpu?.spu === "")
      );

      const isValidNext = nextEntries.every(
        (entry) =>
          entry.status === "R" || entry.status === "" || entry.status === "B"
      );


      if (isValidNext && isValidPrevious) {
        toggleFlow("", currSpu?.id, "SPU", currSpu?.spu, "FWD");
      } else {
        toast.error("Complete Previous");
        console.log("error");
      }
    } else {
      console.log("SME not found in departments");
    }
  }

  const spuRemove = () => {
    if (finalEntry?.id > 0) {
      deleteSpu(finalEntry?.id)
        .then((data) => {
          setStartOrFinish(false);
          toggleFlow("", currSpu?.id, "SPU", currSpu?.spu, "RVS");
          clearData();
        })
        .catch((error) => {
          console.log(error);
        });
    }
    toggleSpuFlowMod();
  };

  useEffect(() => {
    if (finalEntry?.prijscode !== manualPrijscode) {
      setManualPrijscode(finalEntry?.prijscode || "");
    }
  }, [finalEntry?.prijscode, manualPrijscode]);

  useEffect(() => {
    if (finalEntry?.afdeling !== manualAfdeling) {
      setManualAfdeling(finalEntry?.afdeling || "");
    }
  }, [finalEntry?.afdeling, manualAfdeling]);

  const handleChange = (event) => {
    const { id, type } = event.target;
    const isSelected = type === "radio" ? true : event.target.checked;
    const val = isSelected ? "JA" : "NEE";
    switch (id) {
      case "natLakken":
        setFinalEntry({
          ...finalEntry,
          natLakken: val,
          poedercoaten: "NEE",
          stralen: "NEE",
          stralenGedeeltelijk: "NEE",
          schooperen: "NEE",
          kitten: "NEE",
          primer: "NEE",
          ontlakken: "NEE",
          aflakken: "NEE",
          blankeLak: "NEE",
        });
        setCheckedNatLakkenn(val);
        setCheckedStralen("NEE");
        setCheckedStralenGedeeltelijk("NEE");
        setCheckedSchooperen("NEE");
        setCheckedKitten("NEE");
        setCheckedPrimer("NEE");
        setCheckedOntlakken("NEE");
        setCheckedAflakken("NEE");
        setCheckedBlankelak("NEE");
        setSelectedRadio("natLakken");
        break;
      case "poedercoaten":
        setFinalEntry({
          ...finalEntry,
          poedercoaten: val,
          natLakken: "NEE",
          stralen: "NEE",
          stralenGedeeltelijk: "NEE",
          schooperen: "NEE",
          kitten: "NEE",
          primer: "NEE",
          ontlakken: "NEE",
          aflakken: "NEE",
          blankeLak: "NEE",
        });
        setCheckedPoedercoaten(val);
        setCheckedStralen("NEE");
        setCheckedStralenGedeeltelijk("NEE");
        setCheckedSchooperen("NEE");
        setCheckedKitten("NEE");
        setCheckedPrimer("NEE");
        setCheckedOntlakken("NEE");
        setCheckedAflakken("NEE");
        setCheckedBlankelak("NEE");
        setSelectedRadio("poedercoaten");
        setCheckedCombinations([]);
        break;
      case "Stralen":
        setFinalEntry({ ...finalEntry, stralen: val });
        setCheckedStralen(val);
        break;
      case "StralenGedeeltelijk":
        setFinalEntry({ ...finalEntry, stralenGedeeltelijk: val });
        setCheckedStralenGedeeltelijk(val);
        break;
      case "Schooperen":
        setFinalEntry({ ...finalEntry, schooperen: val });
        setCheckedSchooperen(val);
        break;
      case "Kitten":
        setFinalEntry({ ...finalEntry, kitten: val });
        setCheckedKitten(val);
        break;
      case "Primer":
        setFinalEntry({ ...finalEntry, primer: val });
        setCheckedPrimer(val);
        break;
      case "Ontlakken":
        setFinalEntry({ ...finalEntry, ontlakken: val });
        setCheckedOntlakken(val);
        break;
      case "Aflakken":
        setFinalEntry({ ...finalEntry, aflakken: val });
        setCheckedAflakken(val);
        break;
      case "Blankelak":
        setFinalEntry({ ...finalEntry, blankeLak: val });
        setCheckedBlankelak(val);
        break;
      default:
        break;
    }
    if (type === "checkbox") {
      if (!checkedCombinations.includes(id)) {
        setCheckedCombinations((prev) => [...prev, id]);
      } else {
        setCheckedCombinations((prev) => prev.filter((item) => item !== id));
      }
    }

  };

  const red = colorEntry?.red || 0;
  const green = colorEntry?.green || 0;
  const blue = colorEntry?.blue || 0;

  const backgroundImage = shouldUseWhiteWheelImage({ red, green, blue })
    ? carWheelImageWhite
    : carWheelImage;

  const toggleConfirmation = (val) => {
    if (user.departmentsSet[0].depName === "ADMIN") {
      setConfirmationModal(!confirmationModal);
      if (val === "yes") {
        spuRemove();
        toast.success("Removed Successfully");
      } else if (val === "no") {
      }
    } else {
      toast.error("UnAuthorized Department");
    }
  };

  const handleModalClosed = () => {
    clearData();
  };

  const clearData = () => {
    setColorEntry(null);
    setFinalEntry(null);
    setCheckedPoedercoaten("");
    setCheckedStralen("");
    setCheckedStralenGedeeltelijk("");
    setCheckedSchooperen("");
    setCheckedKitten("");
    setCheckedPrimer("");
    setCheckedOntlakken("");
    setCheckedAflakken("");
    setCheckedBlankelak("");
    setSelectedRadio("");
    setCheckedCombinations([]);
    setManualAfdeling("");
    setManualPrijscode("")
    setSelectedAanValue("")
  };

  return (
    <>
      <Modal
        isOpen={spuPanelModal}
        toggle={toggleSpuFlowMod}
        fullscreen
        unmountOnClose
        onClosed={handleModalClosed}
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
        <ModalHeader toggle={toggleSpuFlowMod}>
          <strong>Toevoegen Spuiterij Order</strong>
        </ModalHeader>
        <ModalBody>
          <OrderModalSummary order={currSpu} />

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 2fr 1fr",
              backgroundColor: "#eeeeee",
              height: "74%",
              gap: "0px",
              padding: "0px",
            }}
          >
            <SpuCoatingFields
              selectedRadio={selectedRadio}
              checkedStates={{
                Stralen: checkedStralen,
                StralenGedeeltelijk: checkedStralenGedeeltelijk,
                Schooperen: checkedSchooperen,
                Kitten: checkedKitten,
                Primer: checkedPrimer,
                Ontlakken: checkedOntlakken,
                Aflakken: checkedAflakken,
                Blankelak: checkedBlankelak,
              }}
              onChange={handleChange}
            />
            <div
              style={{
                backgroundColor: "#eeeeee",
                display: "grid",
                gridTemplateRows: "50% 50%",
                height: "100%",
              }}
            >
              <div
                style={{
                  backgroundColor: "#eeeeee",
                  padding: "10px",
                  borderRadius: "5px",
                  boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
                  display: "flex",
                  flexDirection: "row",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <div style={{ display: "flex", flexDirection: "column" }}>
                  <label style={{ margin: "5px" }}>
                    <strong>Prijscode:</strong>
                  </label>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      margin: "5px",
                    }}
                  >
                    <div
                      style={{
                        position: "relative",
                        flex: "1",
                        marginRight: "5px",
                      }}
                    >
                      <input
                        type="text"
                        value={manualPrijscode}
                        onChange={(e) => {
                          setManualPrijscode(e.target.value);
                          setFinalEntry((prev) => ({
                            ...prev,
                            prijscode: e.target.value,
                          }));
                        }
                        }
                        style={{
                          flex: "1",
                          marginRight: "5px",
                          width: "100%",
                          border: "1px solid #ccc",
                          outline: "none",
                          background: "white",
                          padding: "5px",
                          borderRadius: "3px",
                        }}
                      />
                    </div>
                  </div>
                  <label style={{ margin: "5px" }}>
                    <strong>Afdeling:</strong>
                  </label>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      margin: "5px",
                    }}
                  >
                    <div
                      style={{
                        position: "relative",
                        flex: "1",
                        marginRight: "5px",
                      }}
                    >
                      <input
                        type="text"
                        // value={finalEntry?.afdeling}
                        // onChange={(e) =>
                        //   setFinalEntry({
                        //     ...finalEntry,
                        //     afdeling: e.target.value,
                        //   })
                        // }
                        value={manualAfdeling}
                        onChange={(e) => {
                          setManualAfdeling(e.target.value);
                          setFinalEntry((prev) => ({
                            ...prev,
                            afdeling: e.target.value,
                          }));
                        }}
                        style={{
                          flex: "1",
                          marginRight: "5px",
                          width: "100%",
                          border: "1px solid #ccc",
                          outline: "none",
                          background: "white",
                          padding: "5px",
                          borderRadius: "3px",
                        }}
                      />
                    </div>
                  </div>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                  <label style={{ margin: "5px" }}>
                    <strong></strong>
                  </label>

                </div>

                <div
                  style={{
                    marginRight: "80px",
                    display: "flex",
                    flexDirection: "column",
                  }}
                >
                  <label style={{ margin: "5px" }}>
                    <strong>Ral Code:</strong>
                  </label>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      margin: "5px",
                    }}
                  >
                    <div
                      style={{
                        position: "relative",
                        flex: "1",
                        marginRight: "5px",
                      }}
                    >
                      <select
                        value={colorEntry ? colorEntry.id : ""}
                        style={{
                          width: "100%",
                          border: "1px solid #ccc",
                          outline: "none",
                          background: "white",
                          padding: "5px",
                          borderRadius: "3px",
                        }}
                        onChange={(e) => {
                          const selectedObject = wheelColor.find(
                            (color) => color.id === e.target.value
                          );
                          setColorEntry(selectedObject);
                          setFinalEntry((prevFinalEntry) => ({
                            ...prevFinalEntry,
                            orderNumber: currSpu?.orderNumber,
                            prodNumber: currSpu?.product,
                            regel: currSpu?.regel,
                            ralCode: `${selectedObject?.red || 0},${selectedObject?.green || 0
                              },${selectedObject?.blue || 0}`,
                            kleurOmschrijving: selectedObject?.colorName,
                            aan: selectedAanValue
                          }));
                        }}
                      >
                        <option value=""></option>
                        {wheelColor.map((color) => (
                          <option key={color.id} value={color.id}>
                            {color.id}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <label style={{ margin: "5px" }}>
                    <strong>Kleur Omschrijving:</strong>
                  </label>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      margin: "5px",
                    }}
                  >
                    <div
                      style={{
                        position: "relative",
                        flex: "1",
                        marginRight: "5px",
                      }}
                    >
                      <select
                        value={colorEntry ? colorEntry.id : ""}
                        style={{
                          width: "200px",
                          border: "1px solid #ccc",
                          outline: "none",
                          background: "white",
                          padding: "5px",
                          borderRadius: "3px",
                        }}
                        onChange={(e) => {
                          const selectedObject = wheelColor.find(
                            (color) => color.id === e.target.value
                          );
                          setColorEntry(selectedObject);
                          setFinalEntry((prevFinalEntry) => ({
                            ...prevFinalEntry,
                            orderNumber: currSpu?.orderNumber,
                            prodNumber: currSpu?.product,
                            regel: currSpu?.regel,
                            ralCode: `${selectedObject?.red || 0},${selectedObject?.green || 0
                              },${selectedObject?.blue || 0}`,
                            kleurOmschrijving: selectedObject?.colorName,
                            aan: selectedAanValue
                          }));
                        }}
                      >
                        <option value=""></option>
                        {wheelColor.map((color) => (
                          <option key={color.id} value={color.id}>
                            {color.colorName}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <label style={{ margin: "5px" }}>
                    <strong>Aan:</strong>
                  </label>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      margin: "5px",
                    }}
                  >
                    <div
                      style={{
                        position: "relative",
                        flex: "1",
                        marginRight: "5px",
                      }}
                    >
                      <select
                        value={selectedAanValue ? selectedAanValue : ""}
                        style={{
                          width: "100%",
                          border: "1px solid #ccc",
                          outline: "none",
                          background: "white",
                          padding: "5px",
                          borderRadius: "3px",
                        }}
                        onChange={(e) => {
                          setFinalEntry((prevFinalEntry) => ({
                            ...prevFinalEntry,
                            orderNumber: currSpu?.orderNumber,
                            prodNumber: currSpu?.product,
                            regel: currSpu?.regel,
                            ralCode: `${colorEntry?.red || 0},${colorEntry?.green || 0
                              },${colorEntry?.blue || 0}`,
                            kleurOmschrijving: colorEntry?.colorName,
                            aan: e.target.value
                          }));
                          setSelectedAanValue(e.target.value);
                        }}
                      >
                        <option value="">
                        </option>
                        {aanOptions.map((option, index) => (
                          <option key={index} value={option}>
                            {option}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
              </div>

              <div
                style={{
                  backgroundColor: "#eeeeee",
                  padding: "10px",
                  borderRadius: "5px",
                  boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
                }}
              >
                <label style={{ fontSize: "1rem", marginBottom: "5px" }}>
                  <strong>Opmerking:</strong>
                </label>
                <textarea
                  value={finalEntry?.opmerking}
                  onChange={(e) =>
                    setFinalEntry({
                      ...finalEntry,
                      opmerking: e.target.value,
                    })
                  }
                  style={{
                    width: "100%",
                    height: "70%",
                    border: "1px solid #ccc",
                    outline: "none",
                    background: "white",
                    padding: "10px",
                    borderRadius: "3px",
                    fontSize: "1rem",
                    verticalAlign: "top",
                  }}
                />
              </div>
            </div>

            <div
              style={{
                backgroundColor: colorEntry
                  ? `rgba(${colorEntry.red || 0}, ${colorEntry.green || 0}, ${colorEntry.blue || 0
                  })`
                  : "rgba(0, 0, 0)",
                height: "59%",
                borderRadius: "5px",
                position: "relative",
                backgroundSize: "100%",
                backgroundPosition: "center",
                backgroundRepeat: "no-repeat",
                backgroundImage: `url(${backgroundImage})`,
              }}
            ></div>
          </div>
        </ModalBody>
        <ModalFooter>
          {!startOrFinish && (
            <Button color="primary" onClick={() => saveOrderSpu()}>
              Opslaan
            </Button>
          )}
          {startOrFinish && currSpu?.spu === "R" && (
            <Button
              color="warning"
              onClick={() => updateOrderSpu()}
            >
              Update
            </Button>
          )}
          {startOrFinish && currSpu?.spu === "R" && (user.departmentsSet[0].depName === "ADMIN") && (
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
          {startOrFinish && currSpu?.spu === "Y" && (
            <Button color="dark" onClick={() => flowMove("G")}>
              Afmelden
            </Button>
          )}

          {currSpu?.spu !== "Y" && currSpu?.spu !== "G" && (
            <Button color="secondary" onClick={() => toggleConfirmation()}>
              Verwijder
            </Button>
          )}
        </ModalFooter>
      </Modal>
      <Modal isOpen={confirmationModal} unmountOnClose
        backdrop="static" toggle={toggleConfirmation}>
        <ModalBody style={{ justifyContent: "center" }}>
          Remove SPU data for Order {finalEntry?.orderNumber}
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

export default SpuModal;
