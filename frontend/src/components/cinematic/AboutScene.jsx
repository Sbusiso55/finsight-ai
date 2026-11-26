import React from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Environment, Lightformer, ContactShadows, Float } from '@react-three/drei';
import { EffectComposer, Vignette } from '@react-three/postprocessing';
import * as THREE from 'three';
import { BrandCard } from './CinematicScene';

function Rig() {
  const target = new THREE.Vector3();
  useFrame((state) => {
    const s = Math.min(1, window.scrollY / window.innerHeight);
    target.set(state.pointer.x * 0.3, 0.3 + state.pointer.y * 0.15 + s * 0.6, 6 + s * 1.4);
    state.camera.position.lerp(target, 0.06);
    state.camera.lookAt(0, s * 0.3, 0);
  });
  return null;
}

export default function AboutScene({ lite }) {
  return (
    <Canvas
      dpr={[1, lite ? 1.25 : 1.75]}
      camera={{ position: [0, 0.3, 6], fov: 35 }}
      gl={{ antialias: true, alpha: true }}
      onCreated={({ gl }) => { gl.toneMapping = THREE.AgXToneMapping; gl.toneMappingExposure = 1.1; }}
    >
      <Environment resolution={256} frames={1}>
        <Lightformer form="rect" intensity={3} position={[0, 5, -2]} scale={[10, 1, 1]} rotation-x={Math.PI / 2} />
        <Lightformer form="rect" intensity={2} position={[-5, 1, -1]} scale={[10, 2, 1]} rotation-y={Math.PI / 2} />
        <Lightformer form="rect" intensity={2} position={[5, 1, -1]} scale={[10, 2, 1]} rotation-y={-Math.PI / 2} />
      </Environment>
      <spotLight position={[2, 5, 4]} angle={0.4} penumbra={1} intensity={60} />
      <Rig />
      <Float speed={1.2} rotationIntensity={0.1} floatIntensity={0.3}>
        <BrandCard position={[2.1, 0.5, 0]} scale={0.6} baseYaw={-0.55} />
      </Float>
      <ContactShadows position={[2.1, -1.3, 0]} scale={5} blur={2.6} opacity={0.5} far={3} resolution={lite ? 256 : 512} />
      {!lite && (
        <EffectComposer multisampling={0}>
          <Vignette offset={0.3} darkness={0.7} />
        </EffectComposer>
      )}
    </Canvas>
  );
}