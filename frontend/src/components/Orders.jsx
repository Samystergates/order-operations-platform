import React from "react";
import { useEffect, useRef } from "react";
import { useState } from "react";
import { getCurrentUserDetail } from "../auth";
import { updateOrders, updateOrdersColors } from "../services/order-service";
import { loadAllStickers, printingStickerPdf } from "../services/print-service";
import "../css/ordersStyle.css";
import "react-datepicker/dist/react-datepicker.css";
import { toast } from "react-toastify";
import NavCanv from "./NavCanv";
import { doLogout } from "../auth";
import SmeModal from "./SmeModal";
import SpuModal from "./SpuModal";
import SerModal from "./SerModal";
import {
  applyOrderColorFilters,
  applyOrderSearchFilter,
  isOrderFilterActive,
} from "../utils/orderFilters";
import useLatestCallback from "../hooks/useLatestCallback";
import OrderStatusDot, {
  getGroupedOrderStatus,
  getOrderLineStatus,
} from "./orders/OrderStatusDot";
import OrderBackorderCheckbox from "./orders/OrderBackorderCheckbox";
import OrderActionModals from "./orders/OrderActionModals";
import OrderFlowDropdownItems from "./orders/OrderFlowDropdownItems";
import OrderChildRow from "./orders/OrderChildRow";
import OrderChildTableHeader from "./orders/OrderChildTableHeader";
import OrderRow from "./orders/OrderRow";
import OrderTableHeader from "./orders/OrderTableHeader";
import useOrderNote from "./orders/useOrderNote";
import {
  getNextBackorderCheckedStates,
  getNextBackorderLineCheckedStates,
} from "./orders/orderSelection";
import {
  ORDER_SORT_STATE_KEYS,
  sortOrderRows,
} from "./orders/orderSorting";
import {
  getFlowFieldName,
  getNextFlowStatus,
} from "./orders/orderFlowTransitions";
import { updateTransportFlow } from "./orders/transportFlow";
function Orders({
  order,
  searchTerm,
  isMoreOptionsCanv,
  moreOptionsCanv,
  toggleConfirmation,
  confirmationModal,
  reloadOrders,
  loadOrders,
}) {
  const [uniqueUsers, setUniqueUsers] = useState([]);
  const [user, setUser] = useState(null);
  const [filteredOrder, setFilteredOrder] = useState([order]);
  const [itsOdanum, setItsOdanum] = useState(null);
  const [itsTable, setItsTable] = useState(null);
  const [itsCurrentValue, setItsCurrentValue] = useState(null);



  const [expandedRowIndex, setExpandedRowIndex] = useState(null);
  const [itsChecked, setItsChecked] = useState(false);
  const [itsIndex, setItsIndex] = useState(null);
  const [checkedStates, setCheckedStates] = useState({});
  const [checkedInnerStates, setCheckedInnerStates] = useState({});

  const [stickers, setStickers] = useState([]);
  const [modal, setModal] = useState(false);

  const [filterState, setFilterState] = useState({
    SME: [],
    SPU: [],
    MONLB: [],
    MONTR: [],
    MWE: [],
    SER: [],
    TRA: [],
    EXP: []
  });

  const [colorStateExc] = useState("D");
  const [filterSortStates, setFilterSortStates] = useState({
    orderNumber: "Asc",
    backorder: "Asc",
    user: "Asc",
    deliveryDate: "Asc",
    creationDate: "Asc",
    country: "Asc",
    city: "Asc",
    name: "Asc",
    postcode: "Asc",
  });
  const [currentDepFilter, setCurrentDepFilter] = useState("");

  const [spuPanelModal, setSpuPanelModal] = useState(false);
  const [smePanelModal, setSmePanelModal] = useState(false);
  const [serPanelModal, setSerPanelModal] = useState(false);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [smeDropDownOpen, setSmeDropdownOpen] = useState(false);
  const [spuDropDownOpen, setSpuDropdownOpen] = useState(false);
  const [mlDropDownOpen, setMlDropdownOpen] = useState(false);
  const [mtDropDownOpen, setMtDropdownOpen] = useState(false);
  const [mweDropDownOpen, setMweDropdownOpen] = useState(false);
  const [serDropDownOpen, setSerDropdownOpen] = useState(false);
  const [traDropDownOpen, setTraDropdownOpen] = useState(false);
  const [expDropDownOpen, setExpDropdownOpen] = useState(false);
  const [rowDropDownOpen, setRowDropDownOpen] = useState([]);
  const [rowMainDropDownOpen, setRowMainDropDownOpen] = useState([]);
  const [currSpu, setCurrSpu] = useState(null);
  const [currSme, setCurrSme] = useState(null);
  const [currSer, setCurrSer] = useState(null);
  const [orderX, setOrderX] = useState([order]);
  const orderXBoTempRef = useRef([]);

  const isManual = useRef(false);
  const manualTimer = useRef(null);

  const flowForward = useRef("FWD");
  const flowReverse = useRef("RVS");
  const flowHalt = useRef("HLT");
  const applyFiltersRef = useLatestCallback(applyFilters);
  const [tooltipOpen, setTooltipOpen] = useState(false);

  useEffect(() => {
    setUser(getCurrentUserDetail());
  }, []);

  useEffect(() => {
    applyFiltersRef.current();
  }, [searchTerm, applyFiltersRef]);

  const toggleSmeDropDown = () => setSmeDropdownOpen((prevState) => !prevState);
  const toggleSpuDropDown = () => setSpuDropdownOpen((prevState) => !prevState);
  const toggleMlDropDown = () => setMlDropdownOpen((prevState) => !prevState);
  const toggleMtDropDown = () => setMtDropdownOpen((prevState) => !prevState);
  const toggleMweDropDown = () => setMweDropdownOpen((prevState) => !prevState);
  const toggleSerDropDown = () => setSerDropdownOpen((prevState) => !prevState);
  const toggleTraDropDown = () => setTraDropdownOpen((prevState) => !prevState);
  const toggleExpDropDown = () => setExpDropdownOpen((prevState) => !prevState);

  useEffect(() => {
    setUniqueUsers([...new Set(order?.map((item) => item.user))]?.sort());
    setOrderX(order);
    loadAllOrdersStickers();
  }, [order]);


  useEffect(() => {
    setCheckedStates((prevCheckedStates) => {
      if (isManual.current) {
        return prevCheckedStates;
      }

      return getNextBackorderCheckedStates(orderX, prevCheckedStates);
    });

    setCheckedInnerStates((prevCheckedInnerStates) => {
      if (isManual.current) {
        return prevCheckedInnerStates;
      }

      return getNextBackorderLineCheckedStates(orderX, prevCheckedInnerStates);
    });
  }, [orderX]);

  useEffect(() => {
    applyFiltersRef.current();
  }, [selectedUsers, applyFiltersRef]);

  const updateForTra = (updatedTra) => {
    setOrderX(updatedTra);
  };

  const loadAllOrdersStickers = () => {
    loadAllStickers()
      .then((data) => {
        setStickers([...data]);
        if (data !== null) {
        } else {
          toast.error("In The Works");
        }
      })
      .catch((error) => {
        toast.error("Stickers could not be loaded");

        if (error?.message === "Network Error") {
          doLogout();
          window.location.reload();
        }
      });
  };

  const printingSticker = (key, orderNumber) => {
    printingStickerPdf(key, orderNumber)
      .then((blob) => {
        const blobUrl = URL.createObjectURL(blob);

        const link = document.createElement("a");

        link.href = blobUrl;
        link.download = "Sticker Product: " + key + ".pdf";
        link.click();

        URL.revokeObjectURL(blobUrl);
      })
      .catch((error) => {
        toast.error("Sticker could not be downloaded");
      });
  }

  const isFilterActive = (column) => {
    return isOrderFilterActive(filterState, column);
  };

  const handleColorChange = (column, color) => {
    setFilterState(prev => {
      const colors = prev[column] || [];
      const newColors = colors.includes(color)
        ? colors.filter(c => c !== color)
        : [...colors, color];
      return { ...prev, [column]: newColors };
    });
  };

  function applyFilters() {
    if (currentDepFilter !== "") {
      const sortStateKey = ORDER_SORT_STATE_KEYS[currentDepFilter];
      handleFilterClick(
        currentDepFilter,
        sortStateKey ? filterSortStates[sortStateKey] : undefined
      );
    } else {

      let filteredData;
      filteredData = applyOrderColorFilters(orderX, filterState);

      filteredData = filteredData
        .filter(item => item?.orderNumber)
        .map(item => item.orderNumber);

      filteredData = order.filter(item =>
        filteredData.includes(item.orderNumber)
      );

      filteredData = filteredData?.filter((item) => selectedUsers.includes(item.user));

      filteredData = applyOrderSearchFilter(filteredData, searchTerm);

      setFilteredOrder(filteredData);
    }
  }

  const resetFilterForColumn = (column) => {
    setFilterState(prev => ({ ...prev, [column]: [] }));
  };

  const handleFilterClick = (dep, state) => {

    if (dep === "") return;

    setCurrentDepFilter(dep);

    const depFilteredData = applyOrderColorFilters(orderX, filterState);

    const initFilteredData = depFilteredData?.filter((item) => selectedUsers.includes(item.user));

    const orderNumbersToInitFilter = initFilteredData
      .filter(item => item?.orderNumber)
      .map(item => item.orderNumber);
    let filteredData = order.filter(item =>
      orderNumbersToInitFilter.includes(item.orderNumber)
    );

    const sortStateKey = ORDER_SORT_STATE_KEYS[dep];
    const result = sortOrderRows(
      filteredData,
      dep,
      sortStateKey ? filterSortStates[sortStateKey] : "Asc",
      state
    );
    filteredData = result.rows;
    if (sortStateKey) {
      setFilterSortStates((previous) => ({
        ...previous,
        [sortStateKey]: result.nextDirection,
      }));
    }

    filteredData = applyOrderSearchFilter(filteredData, searchTerm);

    setFilteredOrder(filteredData);
  };

  const updatingOrdersBO = async (flowUpdate, orderDto, table, isLastItem, newCheckedInnerStatesProp) => {

    return updateOrders(flowUpdate, orderDto)
      .then((data) => {
        if (data.length > 0) {
          const newCheckedStates = { ...checkedStates };
          newCheckedStates[itsOdanum] = !newCheckedStates[itsOdanum];
          setCheckedStates(newCheckedStates);

          if (table === "inner") {
            const newCheckedInnerStates = { ...checkedInnerStates };
            newCheckedInnerStates[orderDto.id] = !newCheckedInnerStates[orderDto.id];
            setCheckedInnerStates(newCheckedInnerStates);
          }


          if (table === "inner") {
            const updatedOrderList = orderX.map((items) => {
              if (items.id === orderDto.id) {
                return {
                  ...items,
                  backOrder: itsChecked ? "O" : "B",
                  Departments: data
                    .filter((item) => item.id === orderDto.id)
                    .map((item) => item.Departments),
                };
              }
              return items;
            });
            setOrderX(updatedOrderList);
          } else {
            const updatedOrderList = orderXBoTempRef.current.map((items) => {
              if (items.orderNumber === orderDto.orderNumber) {
                return {
                  ...items,
                  backOrder: itsChecked ? "O" : "B",
                  Departments: data
                    .filter((item) => item.id === orderDto.id)
                    .map((item) => item.Departments),
                };
              }
              return items;
            });
            if (!isLastItem) {
              orderXBoTempRef.current = updatedOrderList;
            } else {
              setCheckedInnerStates(newCheckedInnerStatesProp);
              setOrderX(updatedOrderList);
            }
          }
        } else {
          toast.error("Refreshing, Empty List Received");
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        }
      })
      .catch((error) => {
        toast.error("Failed to Update, logging in again" + error);
        setTimeout(() => {
          doLogout();
          window.location.reload();
        }, 2000);
      });
  };

  const updatingOrdersColorMain = (orderNumber, orderDep, orderStatus, flowVal, dep) => {
    updateOrdersColors(orderNumber, orderDep, orderStatus, flowVal)
      .then((data) => {
        if (data.length > 0) {
          if (data) {
            if (orderX.length !== data.length) {
              setFilteredOrder(data);
              reloadOrders(data);
            }
            setOrderX(data);
            toast.success("Successfully Updated");

            return true;
          }
        } else {
          toast.error("Refreshing, Empty List Received");
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        }
      })
      .catch((error) => {
        toast.error("Failed to Update, logging in again" + error);
        setTimeout(() => {
          doLogout();
          window.location.reload();
        }, 2000);
        return false;
      });
  };

  const updatingOrdersColor = (flowUpdate, orderDto, dep) => {

    updateOrders(flowUpdate, orderDto)
      .then((data) => {
        if (data.length > 0) {
          if (data) {
            if (orderX.length !== data.length) {
              setFilteredOrder(data);
              reloadOrders(data);
            }

            setOrderX(data);
            toast.success("Successfully Updated");

            return true;
          }
        } else {
          toast.error("Refreshing, Empty List Received");
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        }
      })
      .catch((error) => {
        toast.error("Failed to Update, logging in again" + error);
        setTimeout(() => {
          doLogout();
          window.location.reload();
        }, 2000);
        return false;
      });
  };

  const handleCheckboxChange = (currentValue, index, isOutterChecked, table, odanum) => {
    setItsIndex(index);
    setItsChecked(isOutterChecked);
    setItsOdanum(odanum);
    setItsTable(table);
    setItsCurrentValue(currentValue);

    toggle();
  };

  const handleUserFilterApply = (users) => {
    setSelectedUsers(users);
    const initFilteredData = applyOrderColorFilters(orderX, filterState);
    const orderNumbersToInitFilter = initFilteredData
      .filter((item) => item?.orderNumber)
      .map((item) => item.orderNumber);

    let filteredData = order.filter((item) =>
      orderNumbersToInitFilter.includes(item.orderNumber)
    );

    setFilteredOrder(
      filteredData.filter((item) => users.includes(item.user))
    );
  };

  const renderBackorderColumn = (value, odanum, index, table) => {
    const isOuter = table === "outter";
    const checked = isOuter
      ? checkedStates[odanum] || false
      : checkedInnerStates[index] || false;

    return (
      <OrderBackorderCheckbox
        checked={checked}
        value={value}
        isOuter={isOuter}
        onChange={(nextValue) =>
          handleCheckboxChange(nextValue, index, checked, table, odanum)
        }
      />
    );
  };

  const toggle = async (val) => {
    if (
      user.departmentsSet[0].depName === "ADMIN" ||
      user.departmentsSet[0].depName === "Logistiek"
    ) {
      setModal(!modal);
      if (val === "yes") {

        if (itsTable === "outter") {

          orderXBoTempRef.current = [...orderX];

          const matchedOrders = orderX.filter(order =>
            order.orderNumber === itsOdanum && order.backOrder === itsCurrentValue
          );

          const newCheckedInnerStates = { ...checkedInnerStates };

          isManual.current = true;
          if (manualTimer.current) {
            clearTimeout(manualTimer.current);
          }

          for (let i = 0; i < matchedOrders.length; i++) {
            const orderForUpdate = matchedOrders[i];
            const updatedOrderBO = {
              ...orderForUpdate,
              backOrder: itsChecked ? "O" : "B",
            };

            const isLast = i === matchedOrders.length - 1;
            const extraString = isLast ? "last-one" : null;

            newCheckedInnerStates[updatedOrderBO.id] = !newCheckedInnerStates[updatedOrderBO.id];

            setItsIndex(updatedOrderBO.id);
            await updatingOrdersBO(true, updatedOrderBO, itsTable, extraString === "last-one", newCheckedInnerStates);
          }
          manualTimer.current = setTimeout(() => {
            isManual.current = false;
          }, 2500);


        } else {
          const orderForUpdate = orderX.find((order) => order.id === itsIndex);
          const updatedOrderBO = {
            ...orderForUpdate,
            backOrder: itsChecked ? "O" : "B",
          };

          await updatingOrdersBO(true, updatedOrderBO, itsTable, false, null);
        }
        toast.success("BO Successfully Updated");

      } else if (val === "no") {
      }
    } else {
      toast.error("UnAuthorized Department");
    }
  };

  const initToggleFlow = (key, oId, dep, currStatus, flowVal) => {
    if (dep === "SME" && flowVal !== flowHalt.current && currStatus !== "B") {
      const orderForUpdate = orderX.find((item) => item.id === oId);
      const index = orderForUpdate?.departments?.findIndex(
        (entry) => entry.depName === dep
      );
      if (index !== -1) {
        const previousEntries = orderForUpdate?.departments?.slice(0, index);
        const nextEntries = orderForUpdate?.departments?.slice(index + 1);

        const isValidPrevious = previousEntries.every(
          (entry) =>
            entry.status === "G" ||
            entry.status === "" ||
            (entry.status === "B" && entry.depName === "SPU")
        );
        const isValidNext = nextEntries.every(
          (entry) =>
            entry.status === "R" || entry.status === "" || entry.status === "B"
        );

        if (isValidNext && isValidPrevious) {
          toggleSmeFlowMod(key, oId, dep, currStatus);
        } else {
          toast.error("Complete Previous");
        }
      }
    } else if (
      dep === "SPU" &&
      flowVal !== flowHalt.current &&
      currStatus !== "B"
    ) {
      const orderForUpdate = orderX.find((item) => item.id === oId);
      const index = orderForUpdate?.departments?.findIndex(
        (entry) => entry.depName === dep
      );
      if (index !== -1) {
        const previousEntries = orderForUpdate?.departments?.slice(0, index);
        const nextEntries = orderForUpdate?.departments?.slice(index + 1);

        const isValidPrevious = previousEntries.every(
          (entry) =>
            entry.status === "G" ||
            entry.status === "" ||
            ((entry.status === "R" || (entry.status === "Y" && currStatus !== "Y")) &&
              entry.depName === "SME" &&
              (currStatus === "" || currStatus === "R"))
        );
        const isValidNext = nextEntries.every(
          (entry) =>
            entry.status === "R" || entry.status === "" || entry.status === "B"
        );

        if (isValidNext && isValidPrevious) {
          toggleSpuFlowMod(key, oId, dep, currStatus);
        } else {
          toast.error("Complete Previous");
        }
      }
    } else {
      if ((dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats")) {
        toggleFlow(key, oId, dep, currStatus, flowVal);
      }
      if ((dep === "SER" || dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek")) {
        if (dep === "SER" && flowVal !== flowHalt.current && (flowVal !== flowReverse.current && currStatus !== "B")) {
          toggleSerMod(key, oId, dep, currStatus);
        } else {
          toggleFlow(key, oId, dep, currStatus, flowVal);
        }
      }
      if ((dep === "SME" || dep === "SPU") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij")) {
        toggleFlow(key, oId, dep, currStatus, flowVal);
      }
    }
  };

  const toggleFlowTra = (key, oId, dep, currStatus, flowVal) => {
    const { orders, allRequestedOrdersComplete } = updateTransportFlow(
      orderX,
      key,
      dep,
      currStatus,
      flowVal
    );

    if (allRequestedOrdersComplete) {
      const orderIds = key.split(",").map((id) => parseInt(id.trim(), 10));
      const activeOrders = orders.filter((order) => !orderIds.includes(order.id));
      setOrderX(activeOrders);
      setFilteredOrder(activeOrders);
      reloadOrders(activeOrders);
    } else {
      setOrderX(orders);
    }
  };

  const toggleTooltip = (orderNumber, regel) => {
    setTooltipOpen((prev) => ({
      ...prev,
      [`${orderNumber}-${regel}`]: !prev[`${orderNumber}-${regel}`],
    }));
  };

  const toggleBOFlow = (onum, or, dep, currStatus, noteExc, flowVal) => {
    const updatedOrderList = orderX.map((items) => {
      if (items.orderNumber === onum && items.regel === or) {
        return {
          ...items,
          exclamation: currStatus,
          excNote: note,
        };
      }
      return items;
    });

    updatingOrdersColor(
      true,
      updatedOrderList.find(
        (item) => item.orderNumber === onum && item.regel === or
      ), dep
    );
  };

  const {
    isOpen: noteModal,
    note,
    setNote,
    toggle: toggleNoteModal,
    save: handleSaveNote,
  } = useOrderNote((orderNumber, lineNumber, noteValue) => {
    toggleBOFlow(
      orderNumber,
      lineNumber,
      "EXCBO",
      "JA",
      noteValue,
      flowForward
    );
  });

  const toggleFlow = (key, oId, dep, currStatus, flowVal) => {
    if (dep === "SME" || dep === "SPU") {
      const orderForUpdate = orderX.find((item) => item.id === oId);
      const index = orderForUpdate?.departments?.findIndex(
        (entry) => entry.depName === dep
      );
      if (index !== -1) {
        const previousEntries = orderForUpdate?.departments?.slice(0, index);
        const nextEntries = orderForUpdate?.departments?.slice(index + 1);

        const isValidPrevious = previousEntries.every(
          (entry) =>
            entry.status === "G" ||
            entry.status === "" ||
            ((entry.status === "R" || entry.status === "Y") && entry.depName === "SME" && dep === "SPU")
        );
        const isValidNext = nextEntries.every(
          (entry) =>
            entry.status === "R" || entry.status === "" || entry.status === "B"
        );
        if ((isValidNext && isValidPrevious) || currStatus === "B") {
          if (dep === "SME") {
            const foundObject = orderX.find((item) => item.id === oId);
            const smeDepartment = foundObject?.departments?.find(dep => dep.depName === "SME");
            if (currStatus === "") {
              setCurrSme({ ...currSme, sme: "R" });
            }
            if (currStatus === "R") {
              setCurrSme({ ...currSme, sme: "Y" });
            }
            if (currStatus === "Y") {
              setCurrSme({ ...currSme, sme: "G" });
            }
            if (currStatus === "B") {
              setCurrSme({ ...currSme, spu: smeDepartment?.prevStatus })
            }
          }

          if (dep === "SPU") {

            const smeDepartment = currSme?.departments?.find(dep => dep.depName === "SPU");
            if (currStatus === "") {
              setCurrSpu({ ...currSpu, spu: "R" });
            }
            if (currStatus === "R") {
              setCurrSpu({ ...currSpu, spu: "Y" });
            }
            if (currStatus === "Y") {
              setCurrSpu({ ...currSpu, spu: "G" });
            }
            if (currStatus === "B") {
              setCurrSpu({ ...currSpu, spu: smeDepartment?.prevStatus });
            }
          }

          const flowFieldName = getFlowFieldName(dep);

          const updatedOrderList = orderX.map((items) => {
            if (items.id === oId) {
              const updatedDepartments = items.departments.map((val) => {
                if (val.depName === dep) {
                  return {
                    ...val,
                    status: getNextFlowStatus(
                      currStatus,
                      flowVal,
                      val.prevStatus,
                      true
                    ),
                    prevStatus: currStatus,
                  };
                } else {
                  return val;
                }
              });

              return {
                ...items,
                [flowFieldName]: updatedDepartments.find(
                  (val) => val.depName === dep
                )?.status,
                departments: updatedDepartments,
              };
            }

            return items;
          });

          updatingOrdersColor(
            true,
            updatedOrderList.find((item) => item.id === oId), dep
          );
        } else {
          toast.error("Complete Previous");
        }
      } else {
        toast.error("Department not found");
      }
    } else {

      if (dep === "SER") {

        const serDepartment = currSer?.departments?.find(dep => dep.depName === "SER");
        if (currStatus === "") {
          setCurrSer({ ...currSer, ser: "R" });
        }
        if (currStatus === "R") {
          setCurrSer({ ...currSer, ser: "Y" });
        }
        if (currStatus === "Y") {
          setCurrSer({ ...currSer, ser: "G" });
        }
        if (currStatus === "B") {
          setCurrSer({ ...currSer, ser: serDepartment?.prevStatus });
        }
      }

      const orderForON = orderX.find((item) => item.id === oId);
      const ordersWithSameNumber = orderX.filter(
        (item) => item.orderNumber === orderForON.orderNumber
      );

      let allConditionsMet = true;
      for (const orderForUpdate of ordersWithSameNumber) {
        const index = orderForUpdate?.departments?.findIndex(
          (entry) => entry.depName === dep
        );

        if (index !== -1) {
          const previousEntries = orderForUpdate?.departments?.slice(0, index);
          const nextEntries = orderForUpdate?.departments?.slice(index + 1);

          const isValidPrevious = previousEntries.every(
            (entry) =>
              entry.status === "G" ||
              entry.status === "" ||
              (entry.status === "R" && entry.depName === "SME" && dep === "SPU")
          );
          const isValidNext = nextEntries.every(
            (entry) =>
              entry.status === "R" ||
              entry.status === "" ||
              entry.status === "B"
          );
          if (isValidPrevious && isValidNext) {
          } else {
            toast.error("Complete Previous");
            allConditionsMet = false;
            break;
          }
        }
      }

      if (allConditionsMet) {
        const flowFieldName = getFlowFieldName(dep);
        const fieldValue = orderForON[flowFieldName];

        updatingOrdersColorMain(
          orderForON.orderNumber,
          flowFieldName,
          fieldValue,
          flowVal,
          dep
        );
      }
    }
  };

  const toggleSmeFlowMod = (key, oId, dep, currStatus) => {
    setSmePanelModal(!smePanelModal);
    const foundObject = orderX.find((item) => item.id === oId);
    setCurrSme(foundObject);
  };

  const toggleSpuFlowMod = (key, oId, dep, currStatus) => {
    setSpuPanelModal(!spuPanelModal);
    const foundObject = orderX.find((item) => item.id === oId);
    setCurrSpu(foundObject);
  };

  const toggleSerMod = (key, oId, dep, currStatus) => {
    setSerPanelModal(!serPanelModal);
    const foundObject = orderX.find((item) => item.id === oId);
    setCurrSer(foundObject);
  };

  const getColorDot = (oNum, value, oId, dep) => {
    return (
      <OrderStatusDot status={getGroupedOrderStatus(orderX, oNum, dep)} />
    );
  };

  const getChildColorDot = (oNum, value, oId, dep) => {
    return (
      <OrderStatusDot status={getOrderLineStatus(orderX, oId, dep)} />
    );
  };

  const handleRowClick = (ordernNum) => {
    if (expandedRowIndex === ordernNum) {
      setExpandedRowIndex(null);
    } else {
      setExpandedRowIndex(ordernNum);
    }
  };

  const handleColClick = (regel) => {
    setRowDropDownOpen((prevState) => {
      const updatedState = [...prevState];
      updatedState[regel] = !prevState[regel];
      return updatedState;
    });
  };

  const handleColClickMain = (orderNumber) => {
    setRowMainDropDownOpen((prevState) => {
      const updatedState = [...prevState];
      updatedState[orderNumber] = !prevState[orderNumber];
      return updatedState;
    });
  };

  const orderDepON = (onum) => {
    return orderX.find(
      (item) => item.orderNumber === onum && item.isParent === 1
    );
  };

  const renderDropdownItems = (dep, oId, _oNm, key) => (
    <OrderFlowDropdownItems
      dep={dep}
      oId={oId}
      orderKey={key}
      user={user}
      orderX={orderX}
      initToggleFlow={initToggleFlow}
      flowForward={flowForward}
      flowHalt={flowHalt}
      flowReverse={flowReverse}
    />
  );

  //Main orders Page view data
  return (
    <>
      <div className="table-container">
        <table>
          <OrderTableHeader
            filterState={filterState}
            filterSortStates={filterSortStates}
            departmentDropdowns={{
              sme: { isOpen: smeDropDownOpen, toggle: toggleSmeDropDown },
              spu: { isOpen: spuDropDownOpen, toggle: toggleSpuDropDown },
              ml: { isOpen: mlDropDownOpen, toggle: toggleMlDropDown },
              mt: { isOpen: mtDropDownOpen, toggle: toggleMtDropDown },
              mwe: { isOpen: mweDropDownOpen, toggle: toggleMweDropDown },
              ser: { isOpen: serDropDownOpen, toggle: toggleSerDropDown },
              tra: { isOpen: traDropDownOpen, toggle: toggleTraDropDown },
              exp: { isOpen: expDropDownOpen, toggle: toggleExpDropDown },
            }}
            userFilter={{
              uniqueUsers,
              selectedUsers,
              onSelectedUsersChange: setSelectedUsers,
              onApply: handleUserFilterApply,
            }}
            colorStateExc={colorStateExc}
            handleColorChange={handleColorChange}
            applyFilters={applyFilters}
            resetFilterForColumn={resetFilterForColumn}
            isFilterActive={isFilterActive}
            handleFilterClick={handleFilterClick}
          />
          <tbody>
            {filteredOrder
              ?.filter((item) => item.isParent === 1)
              .map((item, index) => (
                <React.Fragment key={`${item.id},${index}`}>
                  {item.isParent === 1 && (
                    <OrderRow
                      item={item}
                      isExpanded={expandedRowIndex === item.orderNumber}
                      rowMainDropDownOpen={rowMainDropDownOpen}
                      user={user}
                      orderX={orderX}
                      tooltipOpen={tooltipOpen}
                      flowReverse={flowReverse}
                      handleColClickMain={handleColClickMain}
                      handleRowClick={handleRowClick}
                      renderBackorderColumn={renderBackorderColumn}
                      getColorDot={getColorDot}
                      renderDropdownItems={renderDropdownItems}
                      orderDepON={orderDepON}
                      toggleNoteModal={toggleNoteModal}
                      toggleBOFlow={toggleBOFlow}
                      toggleTooltip={toggleTooltip}
                    />
                  )}

                  {expandedRowIndex === item.orderNumber &&
                    item.isParent === 1 && (
                      <tr>
                        <td className="inner-td" colSpan={22}>
                          <table>
                            <OrderChildTableHeader />
                            <tbody>
                              {filteredOrder.filter((itemC) => item.orderNumber === itemC.orderNumber)
                                .sort((a, b) => a.regel - b.regel)
                                                                .map((itemC) =>
                                  item.isParent === 1 && (
                                    <OrderChildRow
                                      key={`${itemC.orderNumber},${itemC.regel}`}
                                      item={itemC}
                                      rowDropDownOpen={rowDropDownOpen}
                                      handleColClick={handleColClick}
                                      renderBackorderColumn={renderBackorderColumn}
                                      getChildColorDot={getChildColorDot}
                                      renderDropdownItems={renderDropdownItems}
                                      stickers={stickers}
                                      printingSticker={printingSticker}
                                    />
                                  )
                                )}
                            </tbody>
                          </table>
                        </td>
                      </tr>
                    )}
                </React.Fragment>
              ))}
          </tbody>
        </table>

        <OrderActionModals
          backorderModal={modal}
          confirmationModal={confirmationModal}
          noteModal={noteModal}
          backorderOrderNumber={itsOdanum}
          note={note}
          onNoteChange={setNote}
          onToggleBackorder={toggle}
          onToggleConfirmation={toggleConfirmation}
          onToggleNote={toggleNoteModal}
          onSaveNote={handleSaveNote}
        />

        <SmeModal
          smePanelModal={smePanelModal}
          toggleSmeFlowMod={toggleSmeFlowMod}
          currSme={currSme}
          toggleFlow={toggleFlow}
        />

        <SpuModal
          spuPanelModal={spuPanelModal}
          toggleSpuFlowMod={toggleSpuFlowMod}
          currSpu={currSpu}
          toggleFlow={toggleFlow}
        />





      </div>

      <SerModal
        serPanelModal={serPanelModal}
        toggleSerMod={toggleSerMod}
        currSer={currSer}
        toggleFlow={toggleFlow}
      />

      <NavCanv
        isMoreOptionsCanv={isMoreOptionsCanv}
        moreOptionsCanv={moreOptionsCanv}
        updateForTra={updateForTra}
        orderX={orderX}
        stickers={stickers}
        toggleFlowTra={toggleFlowTra}
        loadAllOrdersStickers={loadAllOrdersStickers}
      />
    </>
  );
}

export default Orders;
