// // ComboInput.jsx (or inline in same file)
// import React, { useEffect, useRef, useState } from "react";

// function ComboInput({ value, onChange, options, placeholder }) {
//   const [open, setOpen] = useState(false);
//   const ref = useRef(null);

//   // Close menu on outside click
//   useEffect(() => {
//     const handle = (e) => {
//       if (ref.current && !ref.current.contains(e.target)) setOpen(false);
//     };
//     document.addEventListener("mousedown", handle);
//     return () => document.removeEventListener("mousedown", handle);
//   }, []);

//   return (
//     <div ref={ref} style={{ position: "relative", width: 200 }}>
//       <input
//         type="text"
//         value={value}
//         placeholder={placeholder}
//         onChange={(e) => onChange(e.target.value)}
//         style={{
//           width: "100%",
//           padding: "8px 32px 8px 8px",
//           border: "1px solid #ccc",
//           borderRadius: 4,
//         }}
//       />
//       <button
//         type="button"
//         aria-label="Toggle options"
//         onClick={() => setOpen((o) => !o)}
//         style={{
//           position: "absolute",
//           right: 4,
//           top: "50%",
//           transform: "translateY(-50%)",
//           border: "none",
//           background: "transparent",
//           cursor: "pointer",
//           padding: 4,
//           lineHeight: 1,
//           fontSize: 14,
//         }}
//       >
//         ▾
//       </button>
//       {open && (
//         <div
//           style={{
//             position: "absolute",
//             top: "100%",
//             left: 0,
//             right: 0,
//             marginTop: 4,
//             background: "#fff",
//             border: "1px solid #ccc",
//             borderRadius: 6,
//             boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
//             maxHeight: 180,
//             overflowY: "auto",
//             zIndex: 9999,
//           }}
//         >
//           {options.length === 0 ? (
//             <div style={{ padding: "8px 10px", color: "#888" }}>No options</div>
//           ) : (
//             options.map((opt, i) => (
//               <div
//                 key={i}
//                 onMouseDown={(e) => e.preventDefault()}
//                 onClick={() => {
//                   onChange(opt);
//                   setOpen(false);
//                 }}
//                 style={{ padding: "8px 10px", cursor: "pointer" }}
//                 onMouseEnter={(e) => (e.currentTarget.style.background = "#f5f5f5")}
//                 onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
//               >
//                 {opt}
//               </div>
//             ))
//           )}
//         </div>
//       )}

//       {/* {open && (
//         <div
//           style={{
//             position: "absolute",
//             top: "100%",
//             left: 0,
//             right: 0,
//             marginTop: 4,
//             background: "#fff",
//             border: "1px solid #ccc",
//             borderRadius: 6,
//             boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
//             maxHeight: 180,
//             overflowY: "auto",
//             zIndex: 1000,
//           }}
//         >
//           {options.length === 0 ? (
//             <div style={{ padding: "8px 10px", color: "#888" }}>No options</div>
//           ) : (
//             options.map((opt, i) => (
//               <div
//                 key={i}
//                 onMouseDown={(e) => e.preventDefault()} // keep input focus
//                 onClick={() => {
//                   onChange(opt); // override typed text with chosen option
//                   setOpen(false);
//                 }}
//                 style={{ padding: "8px 10px", cursor: "pointer" }}
//                 onMouseEnter={(e) => (e.currentTarget.style.background = "#f5f5f5")}
//                 onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
//               >
//                 {opt}
//               </div>
//             ))
//           )}
//         </div>
//       )} */}
//     </div>
//   );
// }

// export default ComboInput;
// ComboInputPortal.jsx
import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

export default function ComboInput({ value, onChange, options, placeholder }) {
  const [open, setOpen] = useState(false);
  const anchorRef = useRef(null);
  const menuRef = useRef(null);
  const [menuStyle, setMenuStyle] = useState({});

  // Close on outside click
  useEffect(() => {
    const onDocClick = (e) => {
      if (
        !anchorRef.current?.contains(e.target) &&
        !menuRef.current?.contains(e.target)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, []);

  // Position the floating menu
  const positionMenu = () => {
    const r = anchorRef.current?.getBoundingClientRect();
    if (!r) return;
    const gap = 4;
    const width = Math.round(r.width);
    let top = r.bottom + gap;
    let maxHeight = Math.min(240, window.innerHeight - r.bottom - 16);

    // If not enough space below, open upward
    if (maxHeight < 120) {
      maxHeight = Math.min(240, r.top - 16);
      top = Math.max(8, r.top - maxHeight - gap);
    }

    setMenuStyle({
      position: "fixed",
      top,
      left: r.left,
      width,
      zIndex: 20000,
      background: "#fff",
      border: "1px solid #ccc",
      borderRadius: 6,
      boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
      maxHeight,
      overflowY: "auto",
    });
  };

  useLayoutEffect(() => {
    if (open) positionMenu();
  }, [open, value]);

  useEffect(() => {
    if (!open) return;
    const onScroll = () => positionMenu();
    const onResize = () => positionMenu();
    window.addEventListener("scroll", onScroll, true);
    window.addEventListener("resize", onResize);
    return () => {
      window.removeEventListener("scroll", onScroll, true);
      window.removeEventListener("resize", onResize);
    };
  }, [open]);

  return (
    <>
      <div
        ref={anchorRef}
        style={{ position: "relative", width: 200, display: "inline-block" }}
      >
        <input
          type="text"
          value={value}
          placeholder={placeholder}
          onChange={(e) => onChange(e.target.value)}
          style={{
            width: "100%",
            padding: "8px 32px 8px 8px",
            border: "1px solid #ccc",
            borderRadius: 4,
          }}
        />
        <button
          type="button"
          aria-label="Toggle options"
          onClick={() => setOpen((o) => !o)}
          style={{
            position: "absolute",
            right: 4,
            top: "50%",
            transform: "translateY(-50%)",
            border: "none",
            background: "transparent",
            cursor: "pointer",
            padding: 4,
            lineHeight: 1,
            fontSize: 25,
          }}
        >
          ▾
        </button>
      </div>

      {open &&
        createPortal(
          <div ref={menuRef} style={menuStyle}>
            {options.length === 0 ? (
              <div style={{ padding: "8px 10px", color: "#888" }}>No options</div>
            ) : (
              options.map((opt, i) => (
                <div
                  key={i}
                  onMouseDown={(e) => e.preventDefault()}
                  onClick={() => {
                    onChange(opt);
                    setOpen(false);
                  }}
                  style={{ padding: "8px 10px", cursor: "pointer" }}
                  onMouseEnter={(e) =>
                    (e.currentTarget.style.background = "#f5f5f5")
                  }
                  onMouseLeave={(e) =>
                    (e.currentTarget.style.background = "transparent")
                  }
                >
                  {opt}
                </div>
              ))
            )}
          </div>,
          document.body
        )}
    </>
  );
}
