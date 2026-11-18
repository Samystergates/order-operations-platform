import { Button, Modal, ModalBody, ModalHeader, ModalFooter } from "reactstrap";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { toast } from "react-toastify";
import { getCurrentUserDetail } from "../auth";
import CustomOrderFormModal from "./CustomOrderFormModal";

import {
  loadAllTraOrders,
  saveTra,
  updateTra,
  deleteTra,
  updateTraColors,
} from "../services/transportOrder-service";

import {
  loadCustomTraOrders
} from "../services/order-service";

import { printTraExp } from "./PrintUtil";
import DatePicker from "react-datepicker";
import DropdownField, { getSelectedOption } from "./transport/TransportDropdownField";
import {
  deriveTransportColorValue,
  filterAvailableTransportOrders,
  resolveMainRouteOrders,
} from "./transport/transportRouteLogic";
import SavedRoutesModal from "./transport/SavedRoutesModal";
import TransportOrderPicker from "./transport/TransportOrderPicker";
import TransportRouteActions from "./transport/TransportRouteActions";
import useTransportCatalogs from "./transport/useTransportCatalogs";

function TransportModal({
  transportPanelModal,
  toggleTraFlowMod,
  updateForTra,
  orderX,
  orderY,
  toggleFlowTra,
}) {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [user, setUser] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loadedEntry, setLoadedEntry] = useState(false);
  const [unloadedEntry, setUnloadedEntry] = useState(true);
  const [custOrders, setCustOrders] = useState([]);
  const [tempOrders, setTempOrders] = useState([]);
  const [orderMeta, setOrderMeta] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredOrder, setFilteredOrder] = useState([]);
  const [traOrders, setTraOrders] = useState([orderY]);
  const [routeOrders, setRouteOrders] = useState([]);
  const [selectedRow, setSelectedRow] = useState(null);
  const [selectedRouteRowId, setSelectedRouteRowId] = useState(null);
  const [entry, setEntry] = useState(null);
  const [tempEntry, setTempEntry] = useState(null);
  const [confirmationModal, setConfirmationModal] = useState(false);
  const [colorValue, setColorValue] = useState(null);
  const [customModalOpen, setCustomModalOpen] = useState(false);
  const [draggedIndex, setDraggedIndex] = useState(null);
  const [selectedCustomOrder, setSelectedCustomOrder] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [modal, setModal] = useState(false);
  const [saveModal, setSaveModal] = useState(false);

  const [optionEntry, setOptionEntry] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [saveModalType, setSaveModalType] = useState(null);
  const [saveInputValue, setSaveInputValue] = useState("");
  const [saveLoading, setSaveLoading] = useState(false);
  const canManageTransport =
    user?.departmentsSet[0]?.depName === "ADMIN" ||
    user?.departmentsSet[0]?.depName === "Logistiek";
  const {
    optionConfigs,
    routeOptions,
    driverOptions,
    truckOptions,
    trailerOptions,
    reloadAllCatalogs,
  } = useTransportCatalogs();

  const openDeleteModal = useCallback(() => {
    setModal(true);
  }, []);

  const closeDeleteModal = useCallback(() => {
    setModal(false);
    setDeleteTarget(null);
    setDeleteLoading(false);
    setOptionEntry(null);
  }, []);

  const openSaveModal = useCallback((type) => {
    setSaveModalType(type);
    setSaveInputValue("");
    setSaveModal(true);
  }, []);

  const closeSaveModal = useCallback(() => {
    setSaveModal(false);
    setSaveModalType(null);
    setSaveInputValue("");
    setSaveLoading(false);
  }, []);

  const handleDropdownSelection = useCallback(
    (field) => (selectedOption) => {
      setEntry((prev) => {
        const base = prev && typeof prev === "object" ? prev : {};
        return {
          ...base,
          [field]: selectedOption ? selectedOption.value : "",
        };
      });
    },
    [setEntry]
  );

  const selectedRouteOption = useMemo(
    () => getSelectedOption(routeOptions, entry?.route),
    [routeOptions, entry?.route]
  );
  const selectedDriverOption = useMemo(
    () => getSelectedOption(driverOptions, entry?.chauffeur),
    [driverOptions, entry?.chauffeur]
  );
  const selectedTruckOption = useMemo(
    () => getSelectedOption(truckOptions, entry?.truck),
    [truckOptions, entry?.truck]
  );
  const selectedTrailerOption = useMemo(
    () => getSelectedOption(trailerOptions, entry?.trailer),
    [trailerOptions, entry?.trailer]
  );

  const handleRouteDelete = useCallback(
    (option) => {
      if (!option?.raw) return;
      setDeleteTarget({ type: "ROUTE", id: option.raw.id });
      setOptionEntry(option.raw.route);
      openDeleteModal();
    },
    [openDeleteModal]
  );
  const handleDriverDelete = useCallback(
    (option) => {
      if (!option?.raw) return;
      setDeleteTarget({ type: "DRIVER", id: option.raw.id });
      setOptionEntry(option.raw.driver);
      openDeleteModal();
    },
    [openDeleteModal]
  );
  const handleTruckDelete = useCallback(
    (option) => {
      if (!option?.raw) return;
      setDeleteTarget({ type: "TRUCK", id: option.raw.id });
      setOptionEntry(option.raw.truck);
      openDeleteModal();
    },
    [openDeleteModal]
  );
  const handleTrailerDelete = useCallback(
    (option) => {
      if (!option?.raw) return;
      setDeleteTarget({ type: "TRAILER", id: option.raw.id });
      setOptionEntry(option.raw.trailer);
      openDeleteModal();
    },
    [openDeleteModal]
  );

  const handleRouteAdd = useCallback(() => {
    openSaveModal("ROUTE");
  }, [openSaveModal]);
  const handleDriverAdd = useCallback(() => {
    openSaveModal("DRIVER");
  }, [openSaveModal]);
  const handleTruckAdd = useCallback(() => {
    openSaveModal("TRUCK");
  }, [openSaveModal]);
  const handleTrailerAdd = useCallback(() => {
    openSaveModal("TRAILER");
  }, [openSaveModal]);

  const dropdownCardStyle = useMemo(
    () => ({
      background: "transparent",
      borderRadius: "0px",
      padding: "0.08rem 0.02rem",
      boxShadow: "none",
      border: "none",
      width: "100%",
      display: "grid",
      gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
      columnGap: "0.35rem",
      rowGap: "0.08rem",
      alignItems: "center",
    }),
    []
  );

  const toggleCustomModal = () => setCustomModalOpen(prev => !prev);

  useEffect(() => {
    if (selectedRouteRowId !== null) {
      const selectedEntry = routeOrders?.find(
        (item) => item?.order?.id === selectedRouteRowId
      );

      setEntry(selectedEntry?.order);
      setTempEntry(selectedEntry?.order);

      setOrderMeta(selectedEntry?.order?.orderIds);


      setTempOrders(resolveMainRouteOrders(selectedEntry?.order, orderY));

      setSelectedDate(new Date(selectedEntry?.order?.routeDate));
      setLoadedEntry((prevLoadedEntry) => !prevLoadedEntry);
      setUnloadedEntry(false);
    }
  }, [selectedRouteRowId, orderY, routeOrders]);

  useEffect(() => {
    setColorValue(deriveTransportColorValue(tempOrders));
  }, [tempOrders]);


  useEffect(() => {
    if (transportPanelModal) {
      setSelectedDate(getNextWorkingDay());
      setEntry((prevEntry) => ({ ...prevEntry, routeDate: getNextWorkingDay() }));
      loadAllRouteOrders();
      setLoadedEntry((prevLoadedEntry) => !prevLoadedEntry);
    }
  }, [transportPanelModal]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setUser(getCurrentUserDetail());

        await reloadAllCatalogs();

        loadCustomTraOrders()
          .then((data) => {
            setCustOrders([...data]);
            if (data !== null) {
            } else {
            }
          })
          .catch((error) => {
            console.log(error);
          });
      } catch (error) {
        console.error(error);
      }
    };

    fetchData();
  }, [reloadAllCatalogs]);

  useEffect(() => {
    setTraOrders(filterAvailableTransportOrders(orderY, routeOrders));
  }, [routeOrders, loadedEntry, orderY]);

  useEffect(() => {
    setFilteredOrder(traOrders);
  }, [traOrders]);

  useEffect(() => {
    if (searchTerm !== null) {
      setFilteredOrder(
        traOrders?.filter((item) =>
          item?.orderNumber
            ?.toLowerCase()
            ?.includes(searchTerm?.toLowerCase())
        )
      );
    }
  }, [searchTerm, traOrders]);

  const handleModalClosed = () => {
    clearData();
  };

  function loadAllRouteOrders() {
    loadAllTraOrders()
      .then((data) => {
        const routeOrdersArray = Object.entries(data).map(([key, value]) => ({
          id: key,
          order: value,
        }));
        setRouteOrders(routeOrdersArray);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function saveOrderTra() {
    if (!entry?.route) {
      toast.error("route required");
      return;
    }
    if (!entry?.routeDate) {
      toast.error("route date required");
      return;
    }
    saveTra(entry)
      .then((data) => {
        setEntry(data);
        setTempEntry(data);
        loadAllRouteOrders();
        setUnloadedEntry(false);
        toast.success("Saved Successfully");

        printTraExp(data?.id);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function updateOrderTra() {
    if (!entry?.route) {
      toast.error("route required");
      return;
    }
    if (!entry?.routeDate) {
      toast.error("route date required");
      return;
    }

    updateTra(entry)
      .then((data) => {
        setEntry(data);
        setTempEntry(data);
        loadAllRouteOrders();
        toast.success("Updated Successfully");


        printTraExp(data?.id);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  function deleteOrderTra() {
    if (!entry?.route) {
      toast.error("route required");
      return;
    }
    if (!entry?.routeDate) {
      toast.error("route date required");
      return;
    }
    deleteTra(entry?.id)
      .then((data) => {
        toast.success("Removed Successfully");
        toggleTraFlowMod();
      })
      .catch((error) => {
        toast.error("Error, Try Logging in again", error);
        console.log(error);
      });
  }

  const toggleConfirmation = (val) => {
    if (user.departmentsSet[0].depName === "ADMIN" || user?.departmentsSet[0]?.depName === "Logistiek") {
      setConfirmationModal(!confirmationModal);
      if (val === "yes") {
        deleteOrderTra();
      } else if (val === "no") {
      }
    } else {
      toast.error("UnAuthorized Department");
    }
  };

  const startTraOrders = async () => {
    const tempRelatedItensArray = tempOrders.flatMap((order) => {
      const correspondingOrderXArray = orderX.filter(
        (orderXItem) => orderXItem.orderNumber === order.orderNumber
      );
      return correspondingOrderXArray;
    });

    const valid = tempRelatedItensArray.every((order) => {
      const traIndex = order.departments.findIndex(
        (entry) => entry.depName === "TRA"
      );

      if (traIndex !== -1) {
        const previousEntries = order.departments.slice(0, traIndex);
        const nextEntries = order.departments.slice(traIndex + 1);

        const isValidPrevious = previousEntries.every(
          (entry) => entry.status === "G" || entry.status === ""
        );
        const isValidNext = nextEntries.every(
          (entry) =>
            entry.status === "R" || entry.status === "" || entry.status === "B"
        );

        if (isValidPrevious && isValidNext) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    });

    if (!valid) {
      toast.error("Complete Previous");
      return;
    }

    const idsArray = tempOrders.map((order) => {
      const correspondingOrderXArray = orderX.filter((orderXItem) =>
        orderXItem.orderNumber === order.orderNumber);

      const correspondingIds = correspondingOrderXArray.map((orderXItem) => orderXItem.id);

      return correspondingIds.length > 0 ? correspondingIds : null;
    }).filter((ids) => ids !== null);

    const idsString = idsArray.flat().join(",");

    if (!tempEntry) {
      updateTraColorsFuncY(idsString);

    } else {
      const normalize = (arr) => arr.map(String).sort();

      const entryIds = normalize(entry.orderIds || []);
      const tempIds = normalize(tempEntry.orderIds || []);

      const sameLength = entryIds.length === tempIds.length;
      const sameIds = sameLength && entryIds.every((id, i) => id === tempIds[i]);

      if (sameIds) {
        updateTraColorsFuncY(idsString);
      } else {
        toast.error("Eerst opslaan"); // Dutch: Save first
      }
    }
  };

  const finishTraOrders = () => {
    const tempRelatedItensArray = tempOrders.flatMap((order) => {
      const correspondingOrderXArray = orderX.filter(
        (orderXItem) => orderXItem.orderNumber === order.orderNumber
      );
      return correspondingOrderXArray;
    });

    const valid = tempRelatedItensArray.every((order) => {
      const traIndex = order.departments.findIndex(
        (entry) => entry.depName === "TRA"
      );

      if (traIndex !== -1) {
        const previousEntries = order.departments.slice(0, traIndex);
        const nextEntries = order.departments.slice(traIndex + 1);
        const isValidPrevious = previousEntries.every(
          (entry) => entry.status === "G" || entry.status === ""
        );

        const isValidNext = nextEntries.every(
          (entry) =>
            entry.status === "R" || entry.status === "" || entry.status === "B"
        );

        if (isValidPrevious && isValidNext) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    });
    if (valid) {
      const idsArray = tempOrders
        .map((order) => {
          const correspondingOrderXArray = orderX.filter(
            (orderXItem) => orderXItem.orderNumber === order.orderNumber
          );
          const correspondingIds = correspondingOrderXArray.map(
            (orderXItem) => orderXItem.id
          );

          return correspondingIds.length > 0 ? correspondingIds : null;
        })
        .filter((ids) => ids !== null);

      const idsString = idsArray.flat().join(",");
      updateTraColorsFuncG(idsString);
    } else {
      toast.error("Complete Previous");
    }
  };

  const updateTraColorsFuncY = (idsString) => {
    updateTraColors(idsString, 0)
      .then((data) => {
        if (data) {
          const updatedTempOrders = tempOrders.map((order) => ({
            ...order,
            tra: "Y",
          }));

          setTempOrders(updatedTempOrders);
          toggleFlowTra(idsString, 0, "TRA", "R", "FWD");

          toast.success("Successfully Updated");
        } else {
          toast.error("Can't Proceed");
        }
      })
      .catch((error) => {
        toast.error("Can't Proceed, Try Loging in Again");
      });
  };

  const updateTraColorsFuncG = async (idsString) => {
    updateTraColors(idsString, entry?.id)
      .then((data) => {
        if (data) {
          const updatedTempOrders = tempOrders.map((order) => ({
            ...order,
            tra: "G",
          }));
          setTempOrders(updatedTempOrders);

          toggleFlowTra(idsString, 0, "TRA", "Y", "FWD");
          deleteOrderTra(entry?.id);
          loadAllRouteOrders();
          toast.success("Successfully Updated");
        } else {
          toast.error("Can't Proceed");
        }
      })
      .catch((error) => {
        console.log(error);
        toast.error("Can't Proceed, Try Loging in Again");
      });
  };

  const moveOrderToRight = () => {
    if (selectedRow && !tempOrders?.includes(selectedRow)) {
      const updatedTempTraOrder = filteredOrder.filter(
        (item) => item?.id !== selectedRow?.id
      );
      const updatedTempTraOrder2 = traOrders.filter(
        (item) => item?.id !== selectedRow?.id
      );

      setOrderMeta((prevMeta) => [
        ...prevMeta,
        {
          orderType: "MAIN",
          orderRowNumber: prevMeta.length + 1,
          orderId: selectedRow.orderId || selectedRow.id,
        },
      ]);
      setTempOrders((prevTempOrders) => {
        const newTempOrders = Array.isArray(prevTempOrders)
          ? [...prevTempOrders, selectedRow]
          : [selectedRow];
        return newTempOrders;
      });
      setFilteredOrder(updatedTempTraOrder);
      setTraOrders(updatedTempTraOrder2);
      setSelectedRow(null);
    }
  };

  const handleCustomOrderDelete = (orderId) => {
    if (!orderId) return;
    setOrderMeta((prev) =>
      prev.filter(
        (entry) => !(entry.orderId === orderId && entry.orderType === "CUSTOM")
      )
    );
    setCustOrders((prev) =>
      prev.filter((entry) => entry.id !== orderId)
    );
  };


  const handleCustomOrderSaved = (savedOrder) => {

    const isInCustOrders = custOrders.some((entry) => entry.id === savedOrder.id);
    const isInOrderMeta = orderMeta.some((entry) => entry.orderId === savedOrder.id);

    if (isInCustOrders && isInOrderMeta) {
      setOrderMeta((prev) => {
        const index = prev.findIndex((entry) => entry.orderId === savedOrder.id);
        if (index !== -1) {
          const updated = [...prev];
          updated[index] = {
            ...updated[index],
            orderType: "CUSTOM",
            orderId: savedOrder.id,
          };
          return updated;
        }
        return prev;
      });

      setCustOrders((prev) => {
        const index = prev.findIndex((entry) => entry.id === savedOrder.id);
        if (index !== -1) {
          const updated = [...prev];
          updated[index] = savedOrder;
          return updated;
        }
        return prev;
      });
    } else {
      setOrderMeta((prev) => [
        ...prev,
        {
          orderType: "CUSTOM",
          orderRowNumber: prev.length + 1,
          orderId: savedOrder.id,
        },
      ]);
      setCustOrders((prev) => [...prev, savedOrder]);
    }
  };


  useEffect(() => {
    setEntry((prevEntry) => ({
      ...prevEntry,
      orderIds: orderMeta,
    }));
  }, [orderMeta]);


  const moveOrderToLeft = () => {
    if (!selectedRow) return;

    if (!Array.isArray(orderMeta) || orderMeta.length === 0) return;

    const metaEntry = orderMeta.find((meta) => meta.orderId === selectedRow.id);
    if (!metaEntry || metaEntry.orderType !== 'MAIN') return;


    if (!filteredOrder.includes(selectedRow)) {

      const updatedFilteredTraOrder = tempOrders?.filter(
        (item) => item?.id !== selectedRow?.id
      );
      setFilteredOrder((prevTempOrders) => {
        const newTempOrders = Array.isArray(prevTempOrders)
          ? [...prevTempOrders, selectedRow]
          : [selectedRow];
        return newTempOrders;
      });
      setTraOrders((prevTempOrders) => {
        const newTempOrders = Array.isArray(prevTempOrders)
          ? [...prevTempOrders, selectedRow]
          : [selectedRow];
        return newTempOrders;
      });
      setTempOrders(updatedFilteredTraOrder);
      setOrderMeta((prevMeta) => {
        const filteredMeta = prevMeta.filter((meta) => meta.orderId !== selectedRow.id);
        const reorderedMeta = filteredMeta.map((meta, index) => ({
          ...meta,
          orderRowNumber: index + 1,
        }));

        return reorderedMeta;
      });
      setSelectedRow(null);
    }
  };



  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleDateChange = (date) => {
    if (isWeekday(date) && date >= new Date()) {
      setEntry({ ...entry, routeDate: date });
      setSelectedDate(date);
    }
  };
  const isWeekend = (date) => {
    const day = date.getDay();
    return day !== 0 && day !== 6;
  };
  const isWeekday = (date) => {
    const day = date.getDay();
    return day >= 1 && day <= 5;
  };
  const getNextWorkingDay = () => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    if (tomorrow.getDay() === 6) {
      tomorrow.setDate(tomorrow.getDate() + 2);
    } else if (tomorrow.getDay() === 0) {
      tomorrow.setDate(tomorrow.getDate() + 1);
    }
    return tomorrow;
  };

  const isSelected = (item) => {
    return selectedRow && selectedRow?.id === item?.id;
  };

  const handleRowClick = (item) => {
    if (!item.product) {
      setIsEditing(true);
      setSelectedCustomOrder(item);
    } else {
      setIsEditing(false);
      setSelectedCustomOrder(null);
    }
    setSelectedRow(item);
  };

  const yourDoubleClickFunction = (item) => {
    setSelectedRow(item);
    moveOrderToRight();
  };

  const clearData = () => {
    setSelectedRow(null);
    setEntry("");
    setTempEntry("");
    setTempOrders([]);
    setSearchTerm("");
    setSelectedDate(getNextWorkingDay());
    setFilteredOrder(null);
    setTraOrders([null]);
    setRouteOrders([null]);
    setSelectedRouteRowId(null);
    setUnloadedEntry(true);
    setOrderMeta([]);
  };

  const selectModelAndClose = (id) => {
    setSelectedRouteRowId(id);
    toggleModal();
  };

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const handleButtonClick = () => {
    toggleModal();
  };

  const handleDeleteConfirm = useCallback(async () => {
    if (!deleteTarget) {
      return;
    }
    const config = optionConfigs[deleteTarget.type];
    if (!config || !config.remove) {
      return;
    }

    setDeleteLoading(true);
    try {
      await config.remove(deleteTarget.id);
      await config.reload();
      toast.success(`${config.label} verwijderd`);
      closeDeleteModal();
    } catch (error) {
      console.error(error);
      toast.error(`Kon ${config.label.toLowerCase()} niet verwijderen`);
      setDeleteLoading(false);
    }
  }, [deleteTarget, optionConfigs, closeDeleteModal]);

  const handleSaveSubmit = useCallback(async () => {
    const trimmedValue = saveInputValue.trim();
    if (!saveModalType || trimmedValue === "") {
      toast.error("Waarde is verplicht");
      return;
    }
    const config = optionConfigs[saveModalType];
    if (!config || !config.create) {
      return;
    }

    setSaveLoading(true);
    try {
      await config.create(trimmedValue);
      await config.reload();
      toast.success(`${config.label} opgeslagen`);
      closeSaveModal();
    } catch (error) {
      console.error(error);
      toast.error(`Kon ${config.label.toLowerCase()} niet opslaan`);
      setSaveLoading(false);
    }
  }, [saveInputValue, saveModalType, optionConfigs, closeSaveModal]);

  return (
    <>
      {/* <div
        onMouseDown={(e) => e.stopPropagation()}
        onMouseUp={(e) => e.stopPropagation()}
      > */}
      <Modal
        isOpen={transportPanelModal}
        toggle={toggleTraFlowMod}
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

        <ModalHeader toggle={toggleTraFlowMod}>
          <strong>Toevoegen Transport Order</strong>
        </ModalHeader>
        <ModalBody>

          <div className="row" style={{ alignItems: "flex-start" }} >
            <div className="col-md-3" style={{}}>
              <div>
                <label htmlFor="searchField">
                  <strong>Verkooporder:</strong>
                </label>
                <input
                  type="text"
                  id="searchField"
                  value={searchTerm}
                  onChange={handleSearch}
                  style={{
                    marginLeft: "10px",
                    width: "40%",
                    padding: "5px",
                    borderRadius: "3px",
                    border: "1px solid #ccc",
                  }}
                />
              </div>
            </div>

            <div className="col-md-3" style={{}}>
              <label htmlFor="calendar">
                <strong>Routedatum:</strong>
              </label>
              <div style={{ position: "relative" }}>
                <DatePicker
                  id="calendar"
                  selected={selectedDate}
                  onChange={handleDateChange}
                  dateFormat="dd/MM/yyyy"
                  minDate={new Date()}
                  filterDate={isWeekend}
                />
                <span
                  style={{
                    cursor: "pointer",
                    position: "absolute",
                    top: "50%",
                    marginLeft: "-20px",
                    transform: "translateY(-50%)",
                    pointerEvents: "none",
                  }}
                >
                  ▼
                </span>
              </div>
            </div>

            <div
              className="col-md-6"
              style={{
                display: "flex",
                justifyContent: "flex-start",
                paddingLeft: "1rem",
              }}
            >
              <div style={dropdownCardStyle}>
                <DropdownField
                  id="route"
                  label="Route"
                  placeholder="Select route"
                  value={selectedRouteOption}
                  options={routeOptions}
                  onChange={handleDropdownSelection("route")}
                  onDeleteOption={handleRouteDelete}
                  onAddOption={handleRouteAdd}
                  selectWidth="100%"
                />
                <DropdownField
                  id="chauffeur"
                  label="Chauffeur"
                  placeholder="Select chauffeur"
                  value={selectedDriverOption}
                  options={driverOptions}
                  onChange={handleDropdownSelection("chauffeur")}
                  onDeleteOption={handleDriverDelete}
                  onAddOption={handleDriverAdd}
                  selectWidth="100%"
                />
                <DropdownField
                  id="truck"
                  label="Truck"
                  placeholder="Select truck"
                  value={selectedTruckOption}
                  options={truckOptions}
                  onChange={handleDropdownSelection("truck")}
                  onDeleteOption={handleTruckDelete}
                  onAddOption={handleTruckAdd}
                  selectWidth="100%"
                />
                <DropdownField
                  id="trailer"
                  label="Trailer"
                  placeholder="Select trailer"
                  value={selectedTrailerOption}
                  options={trailerOptions}
                  onChange={handleDropdownSelection("trailer")}
                  onDeleteOption={handleTrailerDelete}
                  onAddOption={handleTrailerAdd}
                  selectWidth="100%"
                />
              </div>
            </div>
          </div>
          <hr style={{ margin: "1rem 0" }} />

          <TransportOrderPicker
            filteredOrder={filteredOrder}
            orderMeta={orderMeta}
            tempOrders={tempOrders}
            custOrders={custOrders}
            draggedIndex={draggedIndex}
            setDraggedIndex={setDraggedIndex}
            setOrderMeta={setOrderMeta}
            colorValue={colorValue}
            isSelected={isSelected}
            onRowClick={handleRowClick}
            onRowDoubleClick={yourDoubleClickFunction}
            onMoveRight={moveOrderToRight}
            onMoveLeft={moveOrderToLeft}
          />
        </ModalBody>
        <TransportRouteActions
          canManageTransport={canManageTransport}
          unloadedEntry={unloadedEntry}
          colorValue={colorValue}
          onCustomOrder={toggleCustomModal}
          onSave={saveOrderTra}
          onUpdate={updateOrderTra}
          onDelete={toggleConfirmation}
          onExisting={handleButtonClick}
          onStartRoute={startTraOrders}
          onFinishRoute={finishTraOrders}
        />

      </Modal>
      {/* </div> */}


      <CustomOrderFormModal
        isOpen={customModalOpen}
        toggle={toggleCustomModal}
        selectedOrder={selectedCustomOrder}
        isEditing={isEditing}
        onSave={handleCustomOrderSaved}
        onDelete={handleCustomOrderDelete}
      />

      <SavedRoutesModal
        isOpen={isModalOpen}
        toggle={toggleModal}
        routeOrders={routeOrders}
        selectedRouteRowId={selectedRouteRowId}
        onSelectRoute={selectModelAndClose}
      />

      <Modal isOpen={modal} unmountOnClose
        backdrop="static" toggle={closeDeleteModal}>
        <ModalBody style={{ justifyContent: "center" }}>
          Verwijder deze invoer: {optionEntry}
        </ModalBody>
        <ModalFooter style={{ justifyContent: "center" }}>
          <Button
            color="primary"
            onClick={handleDeleteConfirm}
            disabled={deleteLoading}
          >
            {deleteLoading ? "Verwijderen..." : "Ja"}
          </Button>{" "}
          <Button
            color="secondary"
            onClick={closeDeleteModal}
            disabled={deleteLoading}
          >
            Nee
          </Button>
        </ModalFooter>
      </Modal>

      <Modal isOpen={saveModal} unmountOnClose
        backdrop="static" toggle={closeSaveModal}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          top: "25%",
        }}>
        <ModalHeader toggle={closeSaveModal}>
          Voeg {optionConfigs[saveModalType]?.label || ""} toe
        </ModalHeader>
        <ModalBody style={{ justifyContent: "center" }}>
          <div style={{ width: "100%" }}>
            <input
              type="text"
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: "8px",
                border: "1px solid #ced4da",
              }}
              value={saveInputValue}
              placeholder={optionConfigs[saveModalType]?.placeholder || ""}
              onChange={(e) => setSaveInputValue(e.target.value)}
            />
            {optionConfigs[saveModalType]?.helperText && (
              <small style={{ display: "block", marginTop: "0.5rem", color: "#6c757d" }}>
                {optionConfigs[saveModalType]?.helperText}
              </small>
            )}
          </div>
        </ModalBody>
        <ModalFooter style={{ justifyContent: "center" }}>
          <Button
            color="success"
            onClick={handleSaveSubmit}
            disabled={saveLoading}
          >
            {saveLoading ? "Opslaan..." : "Opslaan"}
          </Button>{" "}
          <Button
            color="secondary"
            onClick={closeSaveModal}
            disabled={saveLoading}
          >
            Annuleren
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
}

export default TransportModal;
