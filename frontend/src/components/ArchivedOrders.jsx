import React from 'react'
import { useEffect } from 'react'
import { useState } from 'react'
import { Button, Dropdown, DropdownToggle, DropdownItem, DropdownMenu } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faFilter, faAdd, faSubtract } from '@fortawesome/free-solid-svg-icons';
import '../css/ordersStyle.css'
import {
  applyCombinedOrderFilters,
  paginateOrders,
} from '../utils/orderFilters';
    function ArchivedOrders( {order, searchTerm} ) {
    const [uniqueUsers, setUniqueUsers] = useState([]);
    const [filteredOrder, setFilteredOrder] = useState([order]);
    const [expandedRowIndex, setExpandedRowIndex] = useState(null);
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [dropDownOpen, setDropdownOpen] = useState(false);

    const [currentPage, setCurrentPage] = useState(1);
    const entriesPerPage = 200;

    const paginatedOrders = paginateOrders(
      filteredOrder,
      currentPage,
      entriesPerPage
    );

    const handleNextPage = () => {
      if (currentPage * entriesPerPage < filteredOrder.length) {
        setCurrentPage(currentPage + 1);
      }
    };

    const handlePreviousPage = () => {
      if (currentPage > 1) {
        setCurrentPage(currentPage - 1);
      }
    };

 const toggleDropDown = () => {
    setDropdownOpen(!dropDownOpen);
  };
  useEffect(() => {
    setSelectedUsers([...uniqueUsers]);
  }, [uniqueUsers]);

  useEffect(() => {
    setUniqueUsers( [...new Set(order.map((item) => item.user))].sort());
}, [order])

useEffect(() => {
  setFilteredOrder(
    applyCombinedOrderFilters(order, searchTerm, selectedUsers)
  );
  setCurrentPage(1);
}, [order, searchTerm, selectedUsers])

    const handleCheckboxClick = (event) => {
      event.stopPropagation();
      setSelectedUsers((prevSelectedUsers) => {
        if (event.target.value === 'selectAll') {
          return event.target.checked ? [...uniqueUsers] : [];
        } else {
          return event.target.checked
            ? [...prevSelectedUsers, event.target.value]
            : prevSelectedUsers.filter((selectedUser) => selectedUser !== event.target.value);
        }
      });
    };

    const getColorDot = (oNum, value, oId, dep) => {
      const tempList = order.filter((item) => item.orderNumber === oNum);

      const tempColor = tempList?.map((val) => {
        return val[dep];
      });

      tempColor?.map((val) => {
        if (val === "B") {
          return "B";
        } else if (val === "R") {
          return "R";
        } else if (val === "Y") {
          return "Y";
        } else if (val === "G") {
          return "G";
        } else {
          return val;
        }
      });

      const colorPriority = ["B", "R", "Y", "G"];
      const depColor =
        colorPriority?.find((color) => tempColor?.includes(color)) || "";

      switch (depColor) {
        case "R":
          return <div className="dot red"></div>;
        case "G":
          return <div className="dot green"></div>;
        case "Y":
          return <div className="dot yellow"></div>;
        case "B":
          return <div className="dot blue"></div>;
        default:
          return "";
      }
    };

    const getChildColorDot = (oNum, value, oId, dep) => {
      const orderDep = order.find((item) => item.id === oId);
          const depColor = orderDep[dep];

          switch (depColor) {
            case "R":
              return <div className="dot red"></div>;
            case "G":
              return <div className="dot green"></div>;
            case "Y":
              return <div className="dot yellow"></div>;
            case "B":
              return <div className="dot blue"></div>;
            default:
              return null;
          }
    };


    const handleRowClick = (index) => {
      if (expandedRowIndex === index) {
        setExpandedRowIndex(null);
      } else {
        setExpandedRowIndex(index);
      }
    };

    return (

<div className="table-container">
        <table>
        <thead>
          <tr>
            <th></th>
            <th>Verkooporder</th>
            <th>Ordersoort</th>
            <th>SME</th>
            <th>SPU</th>
            <th>MON LB</th>
            <th>MON TR</th>
            <th>MWE</th>
            <th>SER</th>
            <th>TRA</th>
            <th>EXP</th>
            <th>!</th>
            <th style={{overflow: 'hidden',}}>
              <Dropdown isOpen={dropDownOpen} toggle={toggleDropDown} >
              <DropdownToggle  data-toggle="dropdown"
    tag="span">
              Gebruiker (I)<FontAwesomeIcon icon={faFilter} />
    </DropdownToggle>
    <DropdownMenu container="body" style={{
  maxHeight: '200px',
  overflowY: 'auto'
}}>

    <DropdownItem >
                      <label>
                        <input type="checkbox" style={{ width: '22px', height: '22px' }} value = 'selectAll' onChange={(event) => handleCheckboxClick(event)} checked={selectedUsers.length === uniqueUsers.length} />{' '} -Select All/None
                      </label>
                    </DropdownItem>
                    <div style={{ borderTop: "1px dashed #ccc", margin: "0.5rem 0" }} />
                    {uniqueUsers.map((checkUser, index) => (<>
                   <DropdownItem key={index}>
                   <label>
                     <input type="checkbox" style={{ width: '22px', height: '22px' }} value={checkUser} onChange={(event) => handleCheckboxClick(event)} checked={selectedUsers.includes(checkUser)}/> -{' '}
                     {checkUser}
                   </label>
                 </DropdownItem>
                 <div style={{ borderTop: "1px dashed #ccc", margin: "0.5rem 0" }} /> </>
                  ))}
    </DropdownMenu>
  </Dropdown>
            </th>
            <th>Organisatie</th>
            <th>Naam</th>
            <th>Postcode</th>
            <th>Plaats</th>
            <th>Land</th>
            <th>Leverdatum</th>
            <th>Referentie</th>
            <th>Datum order</th>
            <th>Gebruiker (L)</th>
          </tr>
        </thead>
        <tbody>
        {paginatedOrders
            ?.filter((item) => item.isParent === 1)
            .map((item, index) => (
              <React.Fragment key={`${item.id},${index}`}>
                {item.isParent === 1 && (
              <tr key={item.id}>


                <td style={{color:'light'}} onClick={() => handleRowClick(item.orderNumber)}>{expandedRowIndex === item.orderNumber ? <FontAwesomeIcon icon={faSubtract} /> : <FontAwesomeIcon icon={faAdd} />}</td>
                <td>{item.orderNumber}</td>
                <td>{item.orderType}</td>
                <td> {getColorDot(item.orderNumber, item?.sme, item.id, "sme")}</td>
                <td> {getColorDot(item.orderNumber, item?.spu, item.id, "spu")}</td>
                <td>{getColorDot(
                        item.orderNumber,
                        item.monLb,
                        item.id,
                        "monLb"
                      )}</td>
                <td>{getColorDot(
                        item.orderNumber,
                        item.monTr,
                        item.id,
                        "monTr"
                      )}</td>
               <td>
                      {getColorDot(item.orderNumber, item.mwe, item.id, "mwe")}
                    </td>
                    <td>
                      {getColorDot(item.orderNumber, item.ser, item.id, "ser")}
                    </td>
                    <td>
                      {getColorDot(item.orderNumber, item.tra, item.id, "tra")}
                    </td>
                    <td>
                      {getColorDot(item.orderNumber, item.exp, item.id, "exp")}
                    </td>
                <td>{getColorDot(item.exclamation)}</td>
                <td>{item.user}</td>
                <td>{item.organization}</td>
                <td>{item.customerName}</td>
                <td>{item.postCode}</td>
                <td>{item.city}</td>
                <td>{item.country}</td>
                <td>{item.deliveryDate}</td>
                <td>{item.referenceInfo}</td>
                <td>{item.creationDate}</td>
                <td>{item.verifierUser}</td>
              </tr> )}
              {expandedRowIndex === item.orderNumber && (
                item.isParent === 1 && (
                <tr>

                  <td colSpan={22}>
                    <table>

                      <thead>
                        <tr>
                          <th>Regel</th>
                          <th>SME</th>
                          <th>SPU</th>
                          <th>Aantal</th>
                          <th>Product</th>
                          <th>Omschrijving</th>
                          <th>Leverdatum</th>
                          <th>Gebruiker_I</th>
                          <th>MON LB</th>
                          <th>MON TR</th>
                          <th>MWE</th>
                        </tr>
                      </thead>
                      <tbody>

                      {filteredOrder.map(
                              (itemC, key) =>
                                item.orderNumber === itemC.orderNumber &&
                                item.isParent === 1 && (
                                  <tr
                                    key={`${itemC.orderNumber},${itemC.regel}`}
                                  >
                <td>{itemC.regel}</td>
                <td>
                                      {getChildColorDot(
                                        itemC.orderNumber,
                                        itemC?.sme,
                                        itemC.id,
                                        "sme"
                                      )}
                                    </td>
                                    <td>
                                      {getChildColorDot(
                                        itemC.orderNumber,
                                        itemC?.spu,
                                        itemC.id,
                                        "spu"
                                      )}
                                    </td>
                <td>{itemC.aantal}</td>
                <td>{itemC.product}</td>
                <td>{itemC.omsumin}</td>
                <td>{itemC.deliveryDate}</td>
                <td>{itemC.user}</td>
                <td>
                                      {getChildColorDot(
                                        itemC.orderNumber,
                                        itemC.monLb,
                                        itemC.id,
                                        "monLb"
                                      )}
                                    </td>
                                    <td>
                                      {getChildColorDot(
                                        itemC.orderNumber,
                                        itemC.monTr,
                                        itemC.id,
                                        "monTr"
                                      )}
                                    </td>
                                    <td>
                                      {getChildColorDot(
                                        itemC.orderNumber,
                                        itemC.mwe,
                                        itemC.id,
                                        "mwe"
                                      )}
                                    </td>
                        </tr>
                         ))}
                        </tbody>
                    </table>
                  </td>
                </tr>
                )
              )}

            </React.Fragment>
            ))}
          </tbody>
      </table>

      <div className="pagination-controls">
        <Button disabled={currentPage === 1} onClick={handlePreviousPage}>Previous</Button>
        <span> - </span><span>Page {currentPage}</span><span> - </span>
        <Button disabled={currentPage * entriesPerPage >= filteredOrder.length} onClick={handleNextPage}>Next</Button>
      </div>

      </div>
    )
}

export default ArchivedOrders
