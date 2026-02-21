declare module 'qrcode' {
  const qrcode: {
    toDataURL(
      text: string,
      options?: { width?: number; margin?: number; type?: string }
    ): Promise<string>;
  };
  export default qrcode;
}
