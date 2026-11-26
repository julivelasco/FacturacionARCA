export interface ClienteDTO {
    id: number;
    cuitCuil: string;
    denominacion: string;
    emailContacto: string | null;
    telefonoContacto: string | null;
    celularContacto: string | null;
    nombreCalleDomicilio: string | null;
    numeroCalleDomicilio: string | null;
    activo: boolean;
}