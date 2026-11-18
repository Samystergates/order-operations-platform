import React from "react";
import { useEffect } from "react";
import { useState } from "react";
import { Offcanvas, OffcanvasHeader, OffcanvasBody } from "reactstrap";
import TransportModal from "./TransportModal";
import ZoekKleur from "./ZoekKleur";
import ZoekMachine from "./ZoekMachine";
import ZoekLabel from "./ZoekLabel";
import Print from "./Print";
import { handleDownload } from "../services/order-service";

function NavCanv({ isMoreOptionsCanv, moreOptionsCanv, updateForTra, orderX, toggleFlowTra, stickers, loadAllOrdersStickers }) {
  const [transportPanelModal, setTransportPanelModal] = useState(false);
  const [zKPanelModal, setZKPanelModal] = useState(false);
  const [zMPanelModal, setZMPanelModal] = useState(false);
  const [zLPanelModal, setZLPanelModal] = useState(false);
  const [printPanelModal, setPrintPanelModal] = useState(false);
  const [orderY, setOrderY] = useState([]);
  const [isDownloading, setIsDownloading] = useState(false);
  useEffect(() => {
    const filteredArray = orderX.filter(item => item.isParent === 1);
    setOrderY(filteredArray);
  }, [orderX]);

  const toggleTraFlowMod = () => {
    setTransportPanelModal(!transportPanelModal);
  };

  const toggleZKFlowMod = () => {
    setZKPanelModal(!zKPanelModal);
  };
  const toggleZMFlowMod = () => {
    setZMPanelModal(!zMPanelModal);
  };
  const toggleZLFlowMod = () => {
    setZLPanelModal(!zLPanelModal);
  };
  const togglePrintFlowMod = () => {
    setPrintPanelModal(!printPanelModal);
  };

  const downloadExcel = async () => {
    setIsDownloading(true);
    try {
      await handleDownload();
    } catch (error) {
      console.error('Error downloading the file:', error);
      alert('Failed to download the Excel file. Please try again.');
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <>
      <Offcanvas
        direction="end"
        isOpen={isMoreOptionsCanv}
        toggle={moreOptionsCanv}
      >
        <OffcanvasHeader toggle={moreOptionsCanv}></OffcanvasHeader>
        <OffcanvasBody>

          <div onClick={toggleTraFlowMod} className="nav-item-OffC">
            <span>
              Transport Order
            </span>
          </div>
          <div onClick={toggleZMFlowMod} className="nav-item-OffC">
            <span>
              Zoek Machine gegevens
            </span>
          </div>
          <div onClick={toggleZKFlowMod} className="nav-item-OffC">
            <span>
              Zoek Kleur gegevens
            </span>
          </div>
          <div onClick={toggleZLFlowMod} className="nav-item-OffC">
            <span>
              Zoek Label gegevens
            </span>
          </div>
          <div onClick={togglePrintFlowMod} className="nav-item-OffC">
            <span>
              Print Order
            </span>
          </div>
          <div
            onClick={isDownloading ? null : downloadExcel}
            className={`nav-item-OffC ${isDownloading ? 'disabled' : ''}`}
            style={{
              cursor: isDownloading ? 'not-allowed' : 'pointer',
              opacity: isDownloading ? 0.5 : 1,
            }}
          >
            <span>{isDownloading ? 'Downloading...' : 'Download Excel'}</span>
          </div>
        </OffcanvasBody>
      </Offcanvas>
      <TransportModal
        transportPanelModal={transportPanelModal}
        toggleTraFlowMod={toggleTraFlowMod}
        updateForTra={updateForTra}
        orderX={orderX}
        orderY={orderY}
        toggleFlowTra={toggleFlowTra}
      />
      <ZoekMachine
        zMPanelModal={zMPanelModal}
        toggleZMFlowMod={toggleZMFlowMod}
      />
      <ZoekKleur
        zKPanelModal={zKPanelModal}
        toggleZKFlowMod={toggleZKFlowMod}
      />
      <ZoekLabel
        zLPanelModal={zLPanelModal}
        toggleZLFlowMod={toggleZLFlowMod}
        stickers={stickers}
        loadAllOrdersStickers={loadAllOrdersStickers}
      />
      <Print
        printPanelModal={printPanelModal}
        togglePrintFlowMod={togglePrintFlowMod}
        orderX={orderX}
      />
    </>
  );
}

export default NavCanv;
