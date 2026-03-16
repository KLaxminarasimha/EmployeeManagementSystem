import { B } from "../data/constants";

export default function QRCode() {
  const pat = [
    [1,1,1,1,1,1,1],
    [1,0,0,0,0,0,1],
    [1,0,1,1,1,0,1],
    [1,0,1,0,1,0,1],
    [1,0,1,1,1,0,1],
    [1,0,0,0,0,0,1],
    [1,1,1,1,1,1,1],
  ];
  const mid = [
    [0,1,1,0],
    [1,0,0,1],
    [0,1,0,1],
    [1,1,0,0],
    [0,0,1,1],
    [1,0,1,0],
  ];

  return (
    <svg viewBox="0 0 90 90" width="100" height="100">
      {pat.map((row, r) =>
        row.map((c, col) =>
          c ? (
            <rect
              key={`p${r}${col}`}
              x={col * 10 + 5} y={r * 10 + 5}
              width="8" height="8" rx="1.5"
              fill={B.orange}
            />
          ) : null
        )
      )}
      {mid.map((row, r) =>
        row.map((c, col) =>
          c ? (
            <rect
              key={`m${r}${col}`}
              x={col * 10 + 45} y={r * 10 + 45}
              width="8" height="8" rx="1"
              fill={B.navyL} opacity=".7"
            />
          ) : null
        )
      )}
      <rect x="45" y="5" width="30" height="30" rx="4" fill="none" stroke={B.orange} strokeWidth="2"/>
      <rect x="50" y="10" width="20" height="20" rx="2" fill={B.orange} opacity=".15"/>
      <rect x="55" y="15" width="10" height="10" rx="1" fill={B.orange}/>
    </svg>
  );
}
