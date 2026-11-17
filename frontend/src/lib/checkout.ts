export const SUBTOTAL_CLP = 49990;

export const formatCurrency = (value: number) => value.toLocaleString('es-CL');

export const getTotalWithDelivery = (subtotal: number, delivery: number) => subtotal + delivery;
