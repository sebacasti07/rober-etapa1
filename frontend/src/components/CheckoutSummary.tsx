import type { Quote } from '../types/delivery';
import { formatCurrency, getTotalWithDelivery } from '../lib/checkout';

type CheckoutSummaryProps = {
  subtotal: number;
  selected: Quote | null;
  onPay: () => void;
  canPay: boolean;
};

export default function CheckoutSummary({
  subtotal,
  selected,
  onPay,
  canPay,
}: CheckoutSummaryProps) {
  const deliveryPrice = selected?.priceClp ?? 0;
  const total = getTotalWithDelivery(subtotal, deliveryPrice);

  return (
    <section className="panel">
      <h2>3. Resumen</h2>
      <p>
        Productos <b>${formatCurrency(subtotal)}</b>
      </p>
      <p>
        Despacho <b>${formatCurrency(deliveryPrice)}</b>
      </p>
      <h3>Total: ${formatCurrency(total)}</h3>
      <button onClick={onPay} disabled={!canPay}>
        Pagar con Webpay Plus
      </button>
    </section>
  );
}
