import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Environment, Lightformer, MeshReflectorMaterial, RoundedBox, ContactShadows, PerformanceMonitor } from '@react-three/drei';
import { EffectComposer, Bloom, DepthOfField, Vignette, ToneMapping } from '@react-three/postprocessing';
import { ToneMappingMode } from 'postprocessing';
import * as THREE from 'three';
import ModelSlot from './ModelSlot';

const FLOOR_Y = -1.2;
const EMERALD = '#34D399';

const clamp01 = (v) => Math.min(1, Math.max(0, v));
const range = (p, a, b) => clamp01((p - a) / (b - a));
const ease = (t) => t * t * (3 - 2 * t);
const bounce = (t) => {
  const n = 7.5625, d = 2.75;
  if (t < 1 / d) return n * t * t;
  if (t < 2 / d) { t -= 1.5 / d; return n * t * t + 0.75; }
  if (t < 2.5 / d) { t -= 2.25 / d; return n * t * t + 0.9375; }
  t -= 2.625 / d; return n * t * t + 0.984375;
};
const lerp = THREE.MathUtils.lerp;
const FONT = '"DM Sans", Arial, sans-serif';

function makeTexture(w, h, draw) {
  const canvas = document.createElement('canvas');
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d');
  draw(ctx, w, h);
  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.anisotropy = 8;
  tex.userData.ctx = ctx;
  return tex;
}

function drawCardFront(c, w, h) {
  const g = c.createLinearGradient(0, 0, w, h);
  g.addColorStop(0, '#2A2A2D');
  g.addColorStop(0.5, '#18181A');
  g.addColorStop(1, '#0E0E10');
  c.fillStyle = g;
  c.fillRect(0, 0, w, h);
  for (let y = 0; y < h; y += 2) {
    c.fillStyle = `rgba(255,255,255,${Math.random() * 0.025})`;
    c.fillRect(0, y, w, 1);
  }
  const shine = c.createLinearGradient(0, 0, w, 0);
  shine.addColorStop(0, 'rgba(255,255,255,0)');
  shine.addColorStop(0.5, 'rgba(255,255,255,0.05)');
  shine.addColorStop(1, 'rgba(255,255,255,0)');
  c.fillStyle = shine;
  c.fillRect(0, 0, w, h);

  c.fillStyle = '#E5E5E5';
  c.font = `700 46px ${FONT}`;
  c.fillText('CLEARSIDE', 72, 110);
  c.fillStyle = '#9A9A9A';
  c.font = `500 46px ${FONT}`;
  c.fillText('PAY', 352, 110);

  c.strokeStyle = 'rgba(229,229,229,0.55)';
  c.lineWidth = 5;
  for (let i = 1; i <= 3; i++) {
    c.beginPath();
    c.arc(w - 110, 300, i * 20, -0.8, 0.8);
    c.stroke();
  }

  c.fillStyle = 'rgba(229,229,229,0.75)';
  c.font = `500 36px "Courier New", monospace`;
  c.fillText('••••  ••••  ••••  2026', 72, 470);
  c.font = `600 24px ${FONT}`;
  c.fillStyle = 'rgba(229,229,229,0.5)';
  c.fillText('BUSINESS', 72, 560);
  c.fillText('PLATINUM', w - 210, 560);
}

function drawCardBack(c, w, h) {
  const g = c.createLinearGradient(0, 0, w, h);
  g.addColorStop(0, '#222224');
  g.addColorStop(1, '#0E0E10');
  c.fillStyle = g;
  c.fillRect(0, 0, w, h);
  c.fillStyle = '#050505';
  c.fillRect(0, 70, w, 110);
  c.fillStyle = 'rgba(229,229,229,0.45)';
  c.font = `500 26px ${FONT}`;
  c.fillText('Financial clarity, engineered.', 72, 520);
}

function drawPhone(c, w, h, k) {
  c.fillStyle = '#0F0F10';
  c.fillRect(0, 0, w, h);
  c.fillStyle = '#E5E5E5';
  c.font = `600 24px ${FONT}`;
  c.fillText('09:41', 40, 56);
  c.font = `700 28px ${FONT}`;
  c.fillText('CLEARSIDE PAY', 40, 130);
  c.fillStyle = '#8A8A8A';
  c.font = `500 24px ${FONT}`;
  c.fillText('Net cash position', 40, 210);
  c.fillStyle = '#FFFFFF';
  c.font = `700 62px ${FONT}`;
  c.fillText(`R ${(12450 * k).toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, 40, 285);

  const tiles = [
    ['Revenue', `R ${Math.round(18900 * k).toLocaleString('en-ZA')}`, EMERALD],
    ['Expenses', `R ${Math.round(6450 * k).toLocaleString('en-ZA')}`, '#F87171'],
    ['Margin', `${(65.9 * k).toFixed(1)}%`, '#E5E5E5'],
  ];
  tiles.forEach(([label, value, color], i) => {
    const x = 40 + i * 148;
    c.fillStyle = '#1C1C1E';
    c.fillRect(x, 330, 136, 120);
    c.fillStyle = '#8A8A8A';
    c.font = `500 20px ${FONT}`;
    c.fillText(label, x + 14, 368);
    c.fillStyle = color;
    c.font = `700 24px ${FONT}`;
    c.fillText(value, x + 14, 420);
  });

  c.fillStyle = '#8A8A8A';
  c.font = `500 22px ${FONT}`;
  c.fillText('Income this week', 40, 520);
  const heights = [0.4, 0.55, 0.5, 0.7, 0.62, 0.85, 1];
  heights.forEach((v, i) => {
    const bh = v * 300 * k;
    c.fillStyle = i === heights.length - 1 ? EMERALD : '#2F6F57';
    c.fillRect(48 + i * 62, 880 - bh, 40, bh);
  });
  c.fillStyle = '#2A2A2A';
  c.fillRect(40, 882, w - 80, 2);
}

function drawTerminal(c, w, h, ok) {
  c.fillStyle = ok ? '#0F2A1C' : '#101418';
  c.fillRect(0, 0, w, h);
  c.fillStyle = '#CFD5DA';
  c.font = `600 28px ${FONT}`;
  c.fillText('CLEARSIDE PAY', 30, 50);
  c.fillStyle = '#FFFFFF';
  c.font = `700 80px ${FONT}`;
  c.fillText('R 149,90', 30, 180);
  if (ok) {
    c.fillStyle = EMERALD;
    c.beginPath();
    c.arc(w - 90, 150, 46, 0, Math.PI * 2);
    c.fill();
    c.strokeStyle = '#0F2A1C';
    c.lineWidth = 10;
    c.beginPath();
    c.moveTo(w - 112, 150);
    c.lineTo(w - 95, 168);
    c.lineTo(w - 66, 132);
    c.stroke();
    c.fillStyle = EMERALD;
    c.font = `700 44px ${FONT}`;
    c.fillText('Approved', 30, 290);
  } else {
    c.fillStyle = '#CFD5DA';
    c.font = `500 34px ${FONT}`;
    c.fillText('Tap, insert or swipe', 30, 290);
    c.strokeStyle = '#CFD5DA';
    c.lineWidth = 6;
    for (let i = 1; i <= 3; i++) {
      c.beginPath();
      c.arc(w - 130, 150, i * 18, -0.8, 0.8);
      c.stroke();
    }
  }
}

function drawCarton(c, w, h) {
  c.fillStyle = '#F7F7F5';
  c.fillRect(0, 0, w, h);
  c.fillStyle = '#1F3B63';
  c.fillRect(0, 0, w, h * 0.32);
  c.fillStyle = '#FFFFFF';
  c.font = `800 72px ${FONT}`;
  c.fillText('FRESH', 30, 120);
  c.font = `600 36px ${FONT}`;
  c.fillText('Full cream milk', 30, 180);
  c.fillStyle = '#1F3B63';
  c.font = `700 96px ${FONT}`;
  c.fillText('1L', 30, h * 0.54);
  const x0 = 60;
  const y0 = h * 0.66;
  const bh = h * 0.2;
  let x = x0;
  c.fillStyle = '#111111';
  while (x < w - 60) {
    const bw = 2 + Math.floor(Math.random() * 5);
    if (Math.random() > 0.45) c.fillRect(x, y0, bw, bh);
    x += bw + 1;
  }
  c.font = '500 28px monospace';
  c.fillText('6 001234 567890', x0 + 40, y0 + bh + 38);
}

function drawScanner(c, w, h, ok) {
  c.fillStyle = '#0D1117';
  c.fillRect(0, 0, w, h);
  if (ok) {
    c.fillStyle = '#E5E5E5';
    c.font = `600 26px ${FONT}`;
    c.fillText('Fresh milk 1L', 18, 52);
    c.fillStyle = EMERALD;
    c.font = `700 36px ${FONT}`;
    c.fillText('+24 in stock', 18, 106);
  } else {
    c.fillStyle = '#9AA4AD';
    c.font = `600 28px ${FONT}`;
    c.fillText('Ready to scan', 18, 82);
  }
}

function drawReceipt(c, w, h) {
  c.fillStyle = '#F5F2EA';
  c.fillRect(0, 0, w, h);
  c.fillStyle = '#9B978D';
  for (let i = 0; i < 12; i++) c.fillRect(14, 22 + i * 15, 40 + Math.random() * (w - 70), 4);
  c.fillStyle = '#55524C';
  c.fillRect(14, h - 40, w - 28, 6);
}

const KEYS = [
  [0.0, [0, 0.2, 6], [0, 0, 0]],
  [0.06, [0.4, 0.3, 5.6], [0, 0, 0]],
  [0.2, [7.0, 1.8, 5.6], [7.4, -0.5, 0]],
  [0.27, [7.4, 1.5, 5.1], [7.5, -0.5, 0]],
  [0.4, [8.0, 0.5, 3.1], [7.9, 0.1, 0]],
  [0.46, [8.2, 0.45, 2.8], [7.95, 0.1, 0]],
  [0.6, [15.3, 0.6, 3.5], [15.3, -0.2, 0]],
  [0.66, [15.7, 0.6, 3.2], [15.35, -0.2, 0]],
  [0.8, [23.3, 0.6, 3.6], [23.3, -0.3, 0]],
  [0.86, [23.7, 0.6, 3.3], [23.35, -0.3, 0]],
  [0.93, [12, 5, 13], [12, 0, 0]],
  [1.0, [0, 0.3, 6.2], [0, 0, 0]],
];

function CameraRig({ progress }) {
  const pos = useMemo(() => new THREE.Vector3(), []);
  const tgt = useMemo(() => new THREE.Vector3(), []);
  const tmp = useMemo(() => new THREE.Vector3(), []);
  const look = useRef(new THREE.Vector3());
  useFrame((state) => {
    const p = progress.current;
    let i = 0;
    while (i < KEYS.length - 2 && p > KEYS[i + 1][0]) i++;
    const [p0, c0, t0] = KEYS[i];
    const [p1, c1, t1] = KEYS[i + 1];
    const k = ease(clamp01((p - p0) / (p1 - p0)));
    pos.set(...c0).lerp(tmp.set(...c1), k);
    tgt.set(...t0).lerp(tmp.set(...t1), k);
    pos.x += state.pointer.x * 0.25;
    pos.y += state.pointer.y * 0.15;
    state.camera.position.lerp(pos, 0.07);
    look.current.lerp(tgt, 0.07);
    state.camera.lookAt(look.current);
  });
  return null;
}

function Reveal() {
  const spot = useRef();
  useFrame((state) => {
    const k = ease(clamp01(state.clock.elapsedTime / 3));
    spot.current.intensity = 80 * k;
    state.scene.environmentIntensity = 0.15 + 0.85 * k;
  });
  return <spotLight ref={spot} position={[0, 5, 4]} angle={0.4} penumbra={1} intensity={0} distance={20} />;
}

function Floor({ lite, color }) {
  return (
    <mesh rotation-x={-Math.PI / 2} position={[12, FLOOR_Y, 0]}>
      <planeGeometry args={[70, 30]} />
      {lite ? (
        <meshStandardMaterial color={color} roughness={0.5} metalness={0.3} />
      ) : (
        <MeshReflectorMaterial
          blur={[400, 100]}
          resolution={512}
          mixBlur={1}
          mixStrength={25}
          roughness={0.85}
          depthScale={1.2}
          minDepthThreshold={0.4}
          maxDepthThreshold={1.4}
          color={color}
          metalness={0.6}
          mirror={0.5}
        />
      )}
    </mesh>
  );
}

/** Graphite metal card with platinum lettering. Sits to the side, angled toward the centre. */
export function BrandCard({ position = [1.8, 0.25, 0], scale = 0.62, baseYaw = -0.5 }) {
  const g = useRef();
  const front = useMemo(() => makeTexture(1024, 640, drawCardFront), []);
  const back = useMemo(() => makeTexture(1024, 640, drawCardBack), []);
  useEffect(() => () => { front.dispose(); back.dispose(); }, [front, back]);

  useFrame((state) => {
    const t = state.clock.elapsedTime;
    const intro = ease(clamp01(t / 2.5));
    g.current.rotation.y = baseYaw + Math.sin(t * 0.3) * 0.18 + (1 - intro) * 0.9;
    g.current.rotation.x = -0.18 + Math.sin(t * 0.4) * 0.03;
    g.current.position.y = position[1] + Math.sin(t * 0.8) * 0.04;
  });

  const W = 3.2;
  const H = 2.0;
  return (
    <group ref={g} position={position} scale={scale} rotation-z={0.08}>
      <RoundedBox args={[W, H, 0.04]} radius={0.12} smoothness={6}>
        <meshPhysicalMaterial color="#1A1A1C" metalness={1} roughness={0.35} clearcoat={1} clearcoatRoughness={0.12} />
      </RoundedBox>
      <mesh position={[0, 0, 0.021]}>
        <planeGeometry args={[W - 0.06, H - 0.06]} />
        <meshPhysicalMaterial map={front} metalness={0.6} roughness={0.4} clearcoat={1} clearcoatRoughness={0.08} />
      </mesh>
      <mesh position={[0, 0, -0.021]} rotation-y={Math.PI}>
        <planeGeometry args={[W - 0.06, H - 0.06]} />
        <meshPhysicalMaterial map={back} metalness={0.6} roughness={0.4} clearcoat={1} clearcoatRoughness={0.08} />
      </mesh>
      <RoundedBox args={[0.42, 0.32, 0.012]} radius={0.04} smoothness={3} position={[-0.95, 0.05, 0.03]}>
        <meshStandardMaterial color="#C8C8C8" metalness={1} roughness={0.22} />
      </RoundedBox>
    </group>
  );
}

function MessLayer({ progress, count, geometry, material, restY }) {
  const mesh = useRef();
  const dummy = useMemo(() => new THREE.Object3D(), []);
  const tmp = useMemo(() => new THREE.Vector3(), []);
  const screen = useMemo(() => new THREE.Vector3(8.6, 0.3, 0.1), []);
  const data = useMemo(() => Array.from({ length: count }, () => ({
    rest: new THREE.Vector3(8 + (Math.random() - 0.5) * 4.5, FLOOR_Y + restY + Math.random() * 0.05, (Math.random() - 0.5) * 2.4),
    drop: 3 + Math.random() * 3,
    delay: Math.random() * 0.5,
    spin: [Math.random() * 6, Math.random() * 6, Math.random() * 6],
    restRot: Math.random() * Math.PI * 2,
  })), [count, restY]);

  useFrame(() => {
    const p = progress.current;
    data.forEach((d, i) => {
      const fall = bounce(clamp01((range(p, 0.1, 0.24) - d.delay * 0.5) / 0.5));
      const absorb = ease(clamp01((range(p, 0.3, 0.42) - d.delay * 0.4) / 0.6));
      tmp.copy(d.rest);
      tmp.y += d.drop * (1 - fall);
      tmp.lerp(screen, absorb);
      dummy.position.copy(tmp);
      const air = 1 - fall + absorb;
      dummy.rotation.set(d.spin[0] * air, d.restRot + d.spin[1] * air, d.spin[2] * air);
      dummy.scale.setScalar(fall > 0.001 ? Math.max(1 - absorb, 0.0001) : 0.0001);
      dummy.updateMatrix();
      mesh.current.setMatrixAt(i, dummy.matrix);
    });
    mesh.current.instanceMatrix.needsUpdate = true;
  });

  return <instancedMesh ref={mesh} args={[geometry, material, count]} frustumCulled={false} />;
}

function Mess({ progress, lite }) {
  const a = useMemo(() => {
    const receiptTex = makeTexture(128, 256, drawReceipt);
    return {
      receiptTex,
      receiptGeo: new THREE.BoxGeometry(0.32, 0.004, 0.62),
      receiptMat: new THREE.MeshStandardMaterial({ map: receiptTex, roughness: 0.9 }),
      noteGeo: new THREE.BoxGeometry(0.72, 0.003, 0.34),
      noteMat: new THREE.MeshStandardMaterial({ color: '#7F9C76', roughness: 0.8 }),
      coinGeo: new THREE.CylinderGeometry(0.12, 0.12, 0.022, 40),
      goldMat: new THREE.MeshStandardMaterial({ color: '#C8A74E', metalness: 1, roughness: 0.28 }),
      silverMat: new THREE.MeshStandardMaterial({ color: '#C9C9C9', metalness: 1, roughness: 0.25 }),
    };
  }, []);
  useEffect(() => () => Object.values(a).forEach((x) => x.dispose && x.dispose()), [a]);
  const n = lite ? 0.5 : 1;
  return (
    <>
      <MessLayer progress={progress} count={Math.round(40 * n)} geometry={a.receiptGeo} material={a.receiptMat} restY={0.002} />
      <MessLayer progress={progress} count={Math.round(20 * n)} geometry={a.noteGeo} material={a.noteMat} restY={0.002} />
      <MessLayer progress={progress} count={Math.round(30 * n)} geometry={a.coinGeo} material={a.goldMat} restY={0.011} />
      <MessLayer progress={progress} count={Math.round(30 * n)} geometry={a.coinGeo} material={a.silverMat} restY={0.011} />
    </>
  );
}

function Phone({ progress }) {
  const tex = useMemo(() => makeTexture(512, 1024, (c, w, h) => drawPhone(c, w, h, 0)), []);
  useEffect(() => () => tex.dispose(), [tex]);
  const last = useRef(-1);
  const g = useRef();
  useFrame((state) => {
    const fill = ease(range(progress.current, 0.3, 0.42));
    const step = Math.round(fill * 50);
    if (step !== last.current) {
      last.current = step;
      drawPhone(tex.userData.ctx, 512, 1024, step / 50);
      tex.needsUpdate = true;
    }
    g.current.rotation.y = -0.35 + Math.sin(state.clock.elapsedTime * 0.4) * 0.05;
  });
  return (
    <group ref={g} position={[8.6, 0.06, 0]} rotation-x={-0.05}>
      <ModelSlot
        url="/models/phone.glb"
        scale={1}
        fallback={
          <RoundedBox args={[1.2, 2.45, 0.1]} radius={0.12} smoothness={6}>
            <meshPhysicalMaterial color="#1B1B1D" metalness={0.9} roughness={0.25} clearcoat={1} />
          </RoundedBox>
        }
      />
      <mesh position={[0, 0, 0.052]}>
        <planeGeometry args={[1.08, 2.3]} />
        <meshBasicMaterial map={tex} toneMapped={false} />
      </mesh>
      <mesh position={[0, 0, 0.054]}>
        <planeGeometry args={[1.08, 2.3]} />
        <meshPhysicalMaterial color="#FFFFFF" transparent opacity={0.08} roughness={0} clearcoat={1} />
      </mesh>
    </group>
  );
}

function CardMachine({ progress }) {
  const idle = useMemo(() => makeTexture(512, 384, (c, w, h) => drawTerminal(c, w, h, false)), []);
  const ok = useMemo(() => makeTexture(512, 384, (c, w, h) => drawTerminal(c, w, h, true)), []);
  useEffect(() => () => { idle.dispose(); ok.dispose(); }, [idle, ok]);
  const screen = useRef();
  const card = useRef();
  const wave = useRef();
  const approved = useRef(false);

  useFrame(() => {
    const p = progress.current;
    const tap = ease(range(p, 0.5, 0.6));
    const lift = ease(range(p, 0.64, 0.72));
    card.current.position.set(lerp(0.95, 0, tap), lerp(1.5, 0.4, tap) + lift * 0.7, lerp(0.9, 0.3, tap) + lift * 0.3);
    card.current.rotation.set(lerp(-0.6, 0, tap), lerp(0.5, 0, tap), lerp(0.4, 0, tap));
    const isOk = p > 0.6;
    if (isOk !== approved.current) {
      approved.current = isOk;
      screen.current.map = isOk ? ok : idle;
      screen.current.needsUpdate = true;
    }
    const k = range(p, 0.58, 0.68);
    wave.current.visible = k > 0 && k < 1;
    wave.current.scale.setScalar(0.3 + k * 1.6);
    wave.current.material.opacity = (1 - k) * 0.5;
  });

  const keys = [];
  for (let r = 0; r < 4; r++) for (let col = 0; col < 3; col++) keys.push([-0.22 + col * 0.22, -r * 0.15, '#2B2B2E']);
  keys.push([-0.24, -0.62, '#B91C1C'], [0, -0.62, '#B45309'], [0.24, -0.62, '#15803D']);

  return (
    <group position={[16, FLOOR_Y + 0.82, 0]} rotation={[-0.22, -0.3, 0]}>
      <RoundedBox args={[0.85, 1.7, 0.26]} radius={0.07} smoothness={5}>
        <meshPhysicalMaterial color="#1E1E20" metalness={0.2} roughness={0.35} clearcoat={0.6} />
      </RoundedBox>
      <mesh position={[0, 0.38, 0.131]}>
        <planeGeometry args={[0.72, 0.56]} />
        <meshPhysicalMaterial color="#050505" roughness={0.05} clearcoat={1} />
      </mesh>
      <mesh position={[0, 0.38, 0.133]}>
        <planeGeometry args={[0.66, 0.5]} />
        <meshBasicMaterial ref={screen} map={idle} toneMapped={false} />
      </mesh>
      {keys.map(([x, y, color], i) => (
        <RoundedBox key={i} args={[0.18, 0.11, 0.03]} radius={0.015} smoothness={3} position={[x, y, 0.14]}>
          <meshStandardMaterial color={color} roughness={0.45} />
        </RoundedBox>
      ))}
      <mesh ref={wave} position={[0, 0.38, 0.2]}>
        <torusGeometry args={[0.3, 0.006, 8, 96]} />
        <meshBasicMaterial color="#FFFFFF" transparent toneMapped={false} />
      </mesh>
      <group ref={card}>
        <RoundedBox args={[0.85, 0.54, 0.012]} radius={0.03} smoothness={4}>
          <meshPhysicalMaterial color="#D9D9D9" metalness={1} roughness={0.22} clearcoat={1} />
        </RoundedBox>
        <mesh position={[-0.25, 0.05, 0.008]}>
          <boxGeometry args={[0.12, 0.09, 0.004]} />
          <meshStandardMaterial color="#C8A74E" metalness={1} roughness={0.3} />
        </mesh>
      </group>
    </group>
  );
}

function Scanner({ progress }) {
  const carton = useMemo(() => makeTexture(512, 750, drawCarton), []);
  const idle = useMemo(() => makeTexture(256, 144, (c, w, h) => drawScanner(c, w, h, false)), []);
  const ok = useMemo(() => makeTexture(256, 144, (c, w, h) => drawScanner(c, w, h, true)), []);
  useEffect(() => () => { carton.dispose(); idle.dispose(); ok.dispose(); }, [carton, idle, ok]);
  const laser = useRef();
  const screen = useRef();
  const scanned = useRef(false);

  useFrame((state) => {
    const p = progress.current;
    const on = range(p, 0.72, 0.76) * (1 - range(p, 0.84, 0.86));
    laser.current.visible = on > 0.01;
    laser.current.position.y = -0.33 + Math.sin(state.clock.elapsedTime * 6) * 0.08;
    laser.current.material.opacity = on * (0.75 + Math.random() * 0.25);
    const isOk = p > 0.82;
    if (isOk !== scanned.current) {
      scanned.current = isOk;
      screen.current.map = isOk ? ok : idle;
      screen.current.needsUpdate = true;
    }
  });

  return (
    <group>
      <group position={[24, FLOOR_Y + 0.65, 0]} rotation-y={0.25}>
        <RoundedBox args={[0.9, 1.3, 0.6]} radius={0.03} smoothness={3}>
          <meshStandardMaterial color="#F2F2EF" roughness={0.6} />
        </RoundedBox>
        <mesh position={[0, 0, 0.301]}>
          <planeGeometry args={[0.86, 1.26]} />
          <meshStandardMaterial map={carton} roughness={0.55} />
        </mesh>
        <mesh ref={laser} position={[0, -0.33, 0.305]}>
          <planeGeometry args={[0.8, 0.012]} />
          <meshBasicMaterial color="#FF2A2A" transparent toneMapped={false} />
        </mesh>
      </group>
      <group position={[24.95, 0.15, 1.1]} rotation={[0.1, -0.35, 0.3]}>
        <RoundedBox args={[0.55, 0.3, 0.38]} radius={0.06} smoothness={4}>
          <meshPhysicalMaterial color="#232325" roughness={0.35} clearcoat={0.5} />
        </RoundedBox>
        <RoundedBox args={[0.18, 0.7, 0.22]} radius={0.06} smoothness={4} position={[0.1, -0.42, 0]} rotation-z={0.35}>
          <meshPhysicalMaterial color="#1A1A1C" roughness={0.45} />
        </RoundedBox>
        <mesh position={[-0.276, 0, 0]} rotation-y={-Math.PI / 2}>
          <planeGeometry args={[0.3, 0.2]} />
          <meshPhysicalMaterial color="#5A0A0A" roughness={0.05} clearcoat={1} />
        </mesh>
        <mesh position={[0.05, 0, 0.191]}>
          <planeGeometry args={[0.36, 0.2]} />
          <meshBasicMaterial ref={screen} map={idle} toneMapped={false} />
        </mesh>
      </group>
    </group>
  );
}

export default function CinematicScene({ progress, active, lite, onReady, theme = 'dark' }) {
  const light = theme === 'light';
  const bg = light ? '#EDEDEB' : '#0B0B0C';
  const maxDpr = lite ? 1.25 : 1.75;
  const [dpr, setDpr] = useState(maxDpr);
  return (
    <Canvas
      frameloop={active ? 'always' : 'never'}
      dpr={dpr}
      camera={{ position: [0, 0.2, 6], fov: 35, near: 0.1, far: 60 }}
      gl={{ antialias: true, powerPreference: 'high-performance' }}
      onCreated={({ gl }) => {
        gl.toneMapping = THREE.AgXToneMapping;
        gl.toneMappingExposure = 1.05;
        requestAnimationFrame(() => requestAnimationFrame(() => onReady && onReady()));
      }}
    >
      <PerformanceMonitor onDecline={() => setDpr(1)} onIncline={() => setDpr(maxDpr)} />
      <color attach="background" args={[bg]} />
      <fog attach="fog" args={[bg, 9, 24]} />
      <Environment resolution={256} frames={1}>
        <Lightformer form="rect" intensity={3} position={[0, 5, -2]} scale={[10, 1, 1]} rotation-x={Math.PI / 2} />
        <Lightformer form="rect" intensity={2} position={[-5, 1, -1]} scale={[10, 2, 1]} rotation-y={Math.PI / 2} />
        <Lightformer form="rect" intensity={2} position={[5, 1, -1]} scale={[10, 2, 1]} rotation-y={-Math.PI / 2} />
        <Lightformer form="ring" intensity={1.5} position={[0, 2, 6]} scale={3} />
      </Environment>
      <Reveal />
      <CameraRig progress={progress} />
      <Floor lite={lite} color={light ? '#D9D9D6' : '#141414'} />
      <Mess progress={progress} lite={lite} />
      <Phone progress={progress} />
      <CardMachine progress={progress} />
      <Scanner progress={progress} />
      {!lite && [8.6, 16, 24.3].map((x) => (
        <ContactShadows key={x} position={[x, FLOOR_Y + 0.005, 0]} scale={5} blur={2.5} opacity={light ? 0.35 : 0.6} far={2.5} resolution={512} />
      ))}
      {!lite && (
        <EffectComposer multisampling={0}>
          <DepthOfField focusDistance={0.06} focalLength={0.08} bokehScale={1.8} />
          <Bloom intensity={0.35} luminanceThreshold={0.9} luminanceSmoothing={0.2} mipmapBlur />
          <ToneMapping mode={ToneMappingMode.AGX} />
          <Vignette offset={0.25} darkness={light ? 0.3 : 0.7} />
        </EffectComposer>
      )}
    </Canvas>
  );
}
