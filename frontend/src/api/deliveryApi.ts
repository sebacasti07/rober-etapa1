import type { Place, Quote } from '../types/delivery';

const API = (
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
).replace(/\/+$/, '');

export async function getQuotes(from: Place, to: Place): Promise<Quote[]> {
  const response = await fetch(`${API}/deliveries/quotes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pickupLat: from.lat,
      pickupLng: from.lng,
      pickupAddress: from.address,
      dropoffLat: to.lat,
      dropoffLng: to.lng,
      dropoffAddress: to.address,
    }),
  });

  if (!response.ok) {
    throw new Error('No se pudieron cotizar los envíos');
  }

  return response.json();
}

export async function createPayment(
  orderId: string,
  amount: number,
  quote: Quote,
  from: Place,
  to: Place,
) {
  const response = await fetch(`${API}/payments/webpay/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId,
      amount,
      provider: quote.provider,
      quoteId: quote.quoteId,
      delivery: {
        pickupLat: from.lat,
        pickupLng: from.lng,
        pickupAddress: from.address,
        dropoffLat: to.lat,
        dropoffLng: to.lng,
        dropoffAddress: to.address,
      },
    }),
  });

  if (!response.ok) {
    throw new Error('No se pudo iniciar Webpay');
  }

  return response.json() as Promise<{ url: string; token: string }>;
}

export async function getStatus(orderId: string): Promise<{ orderId: string; status: string }> {
  const response = await fetch(`${API}/deliveries/${orderId}/status`);

  if (!response.ok) {
    throw new Error('No se pudo consultar el estado del pedido');
  }

  return response.json();
}