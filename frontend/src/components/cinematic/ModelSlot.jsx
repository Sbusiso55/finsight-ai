import React, { Suspense, useEffect, useMemo, useState } from 'react';
import { useGLTF } from '@react-three/drei';

const checked = new Map();

const modelExists = (url) => {
  if (!checked.has(url)) {
    checked.set(
      url,
      fetch(url, { method: 'HEAD' })
        .then((res) => {
          const type = res.headers.get('content-type') || '';
          return res.ok && !type.includes('text/html');
        })
        .catch(() => false)
    );
  }
  return checked.get(url);
};

class ModelBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { failed: false };
  }

  static getDerivedStateFromError() {
    return { failed: true };
  }

  render() {
    return this.state.failed ? this.props.fallback : this.props.children;
  }
}

function GLB({ url, scale = 1, position = [0, 0, 0], rotation = [0, 0, 0] }) {
  const { scene } = useGLTF(url);
  const model = useMemo(() => {
    const clone = scene.clone(true);
    clone.traverse((o) => {
      if (o.isMesh) {
        o.castShadow = true;
        o.receiveShadow = true;
      }
    });
    return clone;
  }, [scene]);
  return <primitive object={model} scale={scale} position={position} rotation={rotation} />;
}

export default function ModelSlot({ url, fallback, ...props }) {
  const [status, setStatus] = useState('checking');

  useEffect(() => {
    let alive = true;
    modelExists(url).then((ok) => {
      if (alive) setStatus(ok ? 'ready' : 'missing');
    });
    return () => { alive = false; };
  }, [url]);

  if (status !== 'ready') return fallback;

  return (
    <ModelBoundary fallback={fallback}>
      <Suspense fallback={fallback}>
        <GLB url={url} {...props} />
      </Suspense>
    </ModelBoundary>
  );
}