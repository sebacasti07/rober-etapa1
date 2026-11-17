import { useMemo, useState } from 'react';
import { createPayment, getQuotes } from './api/deliveryApi';
import AddressSelector, { places } from './components/AddressSelector';
import CheckoutSummary from './components/CheckoutSummary';
import DeliveryOptions from './components/DeliveryOptions';
import OrderTracking from './components/OrderTracking';
import { formatCurrency } from './lib/checkout';
import type { Quote } from './types/delivery';

type Place = (typeof places)[number];
type Product = {
  id: string;
  name: string;
  detail: string;
  category: string;
  price: number;
  icon: string;
  badge?: string;
};

const products: Product[] = [
  { id: 'oil', name: 'Aceite sintético 10W-40', detail: '1 litro · Alto rendimiento', category: 'Lubricantes', price: 12990, icon: '🛢️', badge: 'Más vendido' },
  { id: 'helmet', name: 'Casco integral Urban', detail: 'Certificado · Talla ajustable', category: 'Accesorios', price: 54990, icon: '🪖', badge: 'Nuevo' },
  { id: 'brakes', name: 'Pastillas de freno', detail: 'Delanteras · Compuesto cerámico', category: 'Frenos', price: 24990, icon: '🛞' },
  { id: 'chain', name: 'Kit de arrastre reforzado', detail: 'Cadena + piñón + corona', category: 'Transmisión', price: 38990, icon: '⚙️' },
  { id: 'gloves', name: 'Guantes urbanos', detail: 'Protección y agarre · Tallas S–XL', category: 'Accesorios', price: 19990, icon: '🧤' },
  { id: 'spark', name: 'Bujía iridium', detail: 'Encendido preciso · Larga duración', category: 'Motor', price: 8990, icon: '🔩' },
  { id: 'filter', name: 'Filtro de aceite', detail: 'Filtración de alta eficiencia', category: 'Motor', price: 6990, icon: '🔧' },
  { id: 'lights', name: 'Kit luces LED', detail: 'Luz blanca · Bajo consumo', category: 'Accesorios', price: 15990, icon: '💡' },
];

const categories = ['Todos', ...Array.from(new Set(products.map((product) => product.category)))];

export default function App() {
  const [from, setFrom] = useState<Place>(places[0]);
  const [to, setTo] = useState<Place>(places[1]);
  const [cart, setCart] = useState<Record<string, number>>({});
  const [category, setCategory] = useState('Todos');
  const [search, setSearch] = useState('');
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [selected, setSelected] = useState<Quote | null>(null);
  const [loading, setLoading] = useState(false);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState('');
  const [order, setOrder] = useState('');

  const paidOrder = new URLSearchParams(window.location.search).get('orderId');
  const cartItems = products.filter((product) => cart[product.id]);
  const itemCount = Object.values(cart).reduce((sum, quantity) => sum + quantity, 0);
  const subtotal = cartItems.reduce((sum, product) => sum + product.price * cart[product.id], 0);
  const visibleProducts = useMemo(() => products.filter((product) => {
    const matchesCategory = category === 'Todos' || product.category === category;
    const matchesSearch = `${product.name} ${product.detail} ${product.category}`.toLowerCase().includes(search.toLowerCase());
    return matchesCategory && matchesSearch;
  }), [category, search]);

  function updateCart(productId: string, change: number) {
    setCart((current) => {
      const quantity = (current[productId] ?? 0) + change;
      const next = { ...current };
      if (quantity <= 0) delete next[productId];
      else next[productId] = quantity;
      return next;
    });
    setQuotes([]);
    setSelected(null);
  }

  async function quote() {
    if (!itemCount) return;
    setLoading(true);
    setError('');
    try {
      const nextQuotes = await getQuotes(from, to);
      setQuotes(nextQuotes);
      setSelected(nextQuotes[0] ?? null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error al cotizar el despacho');
    } finally {
      setLoading(false);
    }
  }

  function changePlace(setPlace: (place: Place) => void, place: Place) {
    setPlace(place);
    setQuotes([]);
    setSelected(null);
  }

  async function pay() {
    if (!selected || !itemCount || paying) return;
    setPaying(true);
    setError('');
    try {
      const id = 'RM-' + Date.now();
      const paymentResponse = await createPayment(id, subtotal + selected.priceClp, selected, from, to);
      setOrder(id);
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = paymentResponse.url;
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = 'token_ws';
      input.value = paymentResponse.token;
      form.appendChild(input);
      document.body.appendChild(form);
      form.submit();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error al iniciar el pago');
      setPaying(false);
    }
  }

  return (
    <main>
      <div className="topline">
        <span>Despachos rápidos en Santiago</span>
        <span>¿Necesitas ayuda? <b>+56 9 5555 0101</b></span>
      </div>
      <header className="site-header">
        <a className="brand" href="#" aria-label="Rober Motos, inicio">
          <span className="brand-mark">RM</span>
          <span>ROBER <b>MOTOS</b><small>REPUESTOS Y ACCESORIOS</small></span>
        </a>
        <div className="header-note">Tu moto, siempre en marcha.</div>
        <a className="cart-link" href="#carrito">Mi carrito <span>{itemCount}</span></a>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">TODO PARA TU MOTO</p>
          <h1>Encuentra el repuesto.<br /><span>Sigue tu camino.</span></h1>
          <p>Repuestos y accesorios seleccionados para que vuelvas a rodar. Compra fácil, recibe rápido.</p>
          <a className="hero-button" href="#productos">Ver productos <span>↓</span></a>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="hero-disc" />
          <span className="hero-emoji">🏍️</span>
          <span className="hero-badge">CALIDAD<br /><b>EN RUTA</b></span>
        </div>
        <div className="hero-stripe" />
      </section>

      <section className="benefits" aria-label="Beneficios">
        <div><span>✓</span><p><b>Repuestos confiables</b><small>Calidad para cada viaje</small></p></div>
        <div><span>⚡</span><p><b>Despacho rápido</b><small>Recibe hoy en Santiago</small></p></div>
        <div><span>♧</span><p><b>Compra segura</b><small>Paga con Webpay</small></p></div>
      </section>

      <section className="catalog" id="productos">
        <div className="section-heading">
          <div><p className="eyebrow">CATÁLOGO</p><h2>Repuestos para seguir rodando</h2></div>
          <label className="search-box">
            <span aria-hidden="true">⌕</span>
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar repuestos..." aria-label="Buscar repuestos" />
          </label>
        </div>
        <div className="categories" aria-label="Filtrar por categoría">
          {categories.map((item) => (
            <button key={item} className={category === item ? 'category active' : 'category'} onClick={() => setCategory(item)}>{item}</button>
          ))}
        </div>

        {visibleProducts.length ? (
          <div className="product-grid">
            {visibleProducts.map((product) => (
              <article className="product-card" key={product.id}>
                <div className={`product-art art-${product.id}`}>
                  {product.badge && <span className="product-badge">{product.badge}</span>}
                  <span className="product-emoji" aria-hidden="true">{product.icon}</span>
                </div>
                <div className="product-info">
                  <span className="product-category">{product.category}</span>
                  <h3>{product.name}</h3>
                  <p>{product.detail}</p>
                  <div className="product-buy">
                    <strong>${formatCurrency(product.price)}</strong>
                    {cart[product.id] ? (
                      <div className="quantity-control" aria-label={`Cantidad de ${product.name}`}>
                        <button aria-label={`Quitar una unidad de ${product.name}`} onClick={() => updateCart(product.id, -1)}>−</button>
                        <span>{cart[product.id]}</span>
                        <button aria-label={`Agregar una unidad de ${product.name}`} onClick={() => updateCart(product.id, 1)}>+</button>
                      </div>
                    ) : (
                      <button className="add-button" onClick={() => updateCart(product.id, 1)} aria-label={`Agregar ${product.name} al carrito`}>+ Agregar</button>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : <p className="empty-search">No encontramos productos. Prueba con otra búsqueda.</p>}
      </section>

      {itemCount > 0 && (
        <section className="checkout" id="carrito">
          <div className="checkout-title">
            <div><p className="eyebrow">TU COMPRA</p><h2>Carrito y despacho</h2></div>
            <span className="cart-count">{itemCount} {itemCount === 1 ? 'producto' : 'productos'}</span>
          </div>
          <div className="checkout-layout">
            <div className="cart-content">
              <div className="cart-items">
                {cartItems.map((product) => (
                  <div className="cart-item" key={product.id}>
                    <span className={`cart-item-icon art-${product.id}`}>{product.icon}</span>
                    <div className="cart-item-info"><b>{product.name}</b><small>${formatCurrency(product.price)} c/u</small></div>
                    <div className="quantity-control">
                      <button aria-label={`Quitar una unidad de ${product.name}`} onClick={() => updateCart(product.id, -1)}>−</button>
                      <span>{cart[product.id]}</span>
                      <button aria-label={`Agregar una unidad de ${product.name}`} onClick={() => updateCart(product.id, 1)}>+</button>
                    </div>
                    <strong>${formatCurrency(product.price * cart[product.id])}</strong>
                  </div>
                ))}
              </div>
              <div className="delivery-form">
                <h3>¿Dónde lo recibes?</h3>
                <div className="address-fields">
                  <label>Retiro desde<AddressSelector value={from} onChange={(place) => changePlace(setFrom, place)} /></label>
                  <label>Comuna de entrega<AddressSelector value={to} onChange={(place) => changePlace(setTo, place)} /></label>
                </div>
                <button className="quote-button" onClick={quote} disabled={loading}>
                  {loading ? 'Buscando opciones...' : quotes.length ? 'Actualizar opciones de despacho' : 'Ver opciones de despacho'}
                </button>
              </div>
              {quotes.length > 0 && <div className="delivery-options"><h3>Elige tu despacho</h3><DeliveryOptions quotes={quotes} selected={selected?.quoteId ?? ''} onSelect={(quote) => setSelected(quote)} /></div>}
            </div>
            <aside className="summary-wrap">
              <CheckoutSummary subtotal={subtotal} selected={selected} onPay={pay} canPay={Boolean(selected) && !paying} />
              {paying && <p className="payment-note">Conectando con Webpay...</p>}
            </aside>
          </div>
        </section>
      )}

      {error && <p className="error" role="alert">{error}</p>}
      {(order || paidOrder) && <OrderTracking id={order || paidOrder!} />}
      <footer>
        <a className="brand footer-brand" href="#"><span className="brand-mark">RM</span><span>ROBER <b>MOTOS</b><small>REPUESTOS Y ACCESORIOS</small></span></a>
        <p>© 2025 Rober Motos · Repuestos para acompañarte en cada ruta.</p>
      </footer>
    </main>
  );
}
