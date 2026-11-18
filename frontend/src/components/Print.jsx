import { Modal, ModalBody, ModalHeader, ModalFooter } from "reactstrap";
import { useCallback, useEffect, useState } from "react";
import {
  loadAllSme,
  loadAllSpu,
  loadAllTra,
} from "../services/print-service";
import { printSmeExp,printSpuExp,printMonExp ,printTraExp} from "./PrintUtil";
import "../css/print.css";

function Print({ printPanelModal, togglePrintFlowMod, orderX }) {
  const [selectedOrderId, setSelectedOrderId] = useState("");
  const [smeOrders, setSmeOrders] = useState([]);
  const [spuOrders, setSpuOrders] = useState([]);
  const [traOrders, setTraOrders] = useState([]);
  const [monOrders, setMonOrders] = useState([]);
  const [orderY, setOrderY] = useState([null]);
  const [idMapping, setIdMapping] = useState({});

  const loadAllSmeOrders = useCallback(() => {
    loadAllSme()
      .then((data) => {
        const filteredOrders = orderX.filter((order) =>
          data.some(
            (dataItem) =>
              order.orderNumber === dataItem.orderNumber &&
              order.regel === dataItem.regel
          )
        );
        setSmeOrders(filteredOrders);
      })
      .catch((error) => {
        console.log(error);
      });
  }, [orderX]);

  const loadAllSpuOrders = useCallback(() => {
    loadAllSpu()
      .then((data) => {
        const filteredOrders = orderX.filter((order) =>
          data.some(
            (dataItem) =>
              order.orderNumber === dataItem.orderNumber &&
              order.regel === dataItem.regel
          )
        );
        setSpuOrders(filteredOrders);
      })
      .catch((error) => {
        console.log(error);
      });
  }, [orderX]);

  const loadAllTraOrders = useCallback(() => {
    loadAllTra()
      .then((data) => {
        const filteredOrders = orderX.filter((order) => {
          const orderIDString = order.id.toString();
          return data.some((dataItem) => {
            if (
              dataItem &&
              dataItem.orderIds &&
              typeof dataItem.orderIds === "string"
            ) {
              const idsArray = dataItem.orderIds.split(",");
              const includesOrderID = idsArray.includes(orderIDString);

              if (includesOrderID) {
                setIdMapping((prevMapping) => ({
                  ...prevMapping,
                  [orderIDString]: dataItem.id,
                }));
              }
              return includesOrderID;
            }
            return false;
          });
        });
        setTraOrders(filteredOrders);
      })
      .catch((error) => {
        console.log(error);
      });
  }, [orderX]);

  useEffect(() => {
    if(printPanelModal){
      loadAllSmeOrders();
      loadAllSpuOrders();
      loadAllTraOrders();
      const filteredOrders = orderX.filter(
        (order) => order.monTr === "Y" || order.monLb === "Y"
      );
      setMonOrders(filteredOrders);
    }
  }, [loadAllSmeOrders, loadAllSpuOrders, loadAllTraOrders, orderX, printPanelModal]);


  useEffect(() => {
    const mergedOrders = [
      ...smeOrders,
      ...spuOrders,
      ...traOrders,
      ...monOrders,
    ];
    const uniqueOrdersSet = new Set();

    const uniqueMergedOrders = mergedOrders.filter((order) => {
      const key = JSON.stringify({
        orderNumber: order?.orderNumber,
        regel: order?.regel,
      });

      if (!uniqueOrdersSet.has(key)) {
        uniqueOrdersSet.add(key);
        return true;
      }
      return false;
    });

    setOrderY(uniqueMergedOrders);
  }, [smeOrders, spuOrders, traOrders,monOrders]);

  const handleOrderChange = (event) => {
    setSelectedOrderId(event.target.value);
  };

  function printFunc(dep) {
    const foundOrder = orderY.find((order) => String(order?.id) === String(selectedOrderId));

    if (foundOrder) {
    if (dep === "TRA") {
      if (foundOrder?.tra !== "" && foundOrder?.tra !== null) {
        const key = idMapping[selectedOrderId];
        printTra(key);
      }
    }
    if (dep === "SME") {
      if (foundOrder?.sme !== "" && foundOrder?.sme !== null) {
        printSme(foundOrder?.orderNumber+","+foundOrder?.regel);
      }
    }
    if (dep === "SPU") {
      if (foundOrder?.spu !== "" && foundOrder?.spu !== null) {
        printSpu(foundOrder?.orderNumber+","+foundOrder?.regel);
      }
    }
    if (dep === "MON") {
      if (foundOrder?.monTr === "Y" || foundOrder?.monLb === "Y") {
        printMon(foundOrder?.orderNumber);
      }
    }

      }  }

  function printTra(key) {
    printTraExp(key);
  }

  function printSme(key) {
    printSmeExp(key);
  }

  function printSpu(key) {
    printSpuExp(key);
  }

  function printMon(key) {
    printMonExp(key);
  }

  return (
    <Modal
      isOpen={printPanelModal}
      toggle={togglePrintFlowMod}
      fullscreen
      unmountOnClose
      backdrop="static"
      style={{
        maxWidth: "30%",
        maxHeight: "40%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        left: "35%",
        top: "25%",
      }}
    >
      <ModalHeader toggle={togglePrintFlowMod}>
        <strong>Print Order</strong>
      </ModalHeader>
      <ModalBody>
        <div>
          <select
            className="select-container"
            onChange={handleOrderChange}
            value={selectedOrderId}
          >
            <option value="" disabled>
              - Select Order Number -
            </option>
            {orderY.map((order) => (
              <option key={order?.id} value={order?.id}>
                {`${order?.orderNumber} - ${order?.regel}`}
              </option>
            ))}
          </select>

          <hr style={{ margin: "1rem 0" }} />

          <div className="button-container">
            <div className="custom-button" onClick={() => printFunc("TRA")}>
              Print TRA
            </div>
            <div className="custom-button" onClick={() => printFunc("SME")}>
              Print SME
            </div>
            <div className="custom-button" onClick={() => printFunc("SPU")}>
              Print SPU
            </div>
            <div className="custom-button" onClick={() => printFunc("MON")}>
              Print MON
            </div>
          </div>
        </div>
      </ModalBody>
      <ModalFooter></ModalFooter>
    </Modal>
  );
}

export default Print;
