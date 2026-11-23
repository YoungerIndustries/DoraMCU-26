/**
 * Jest setup file – polyfills TextEncoder/TextDecoder for jsdom.
 */
const { TextEncoder, TextDecoder } = require('util');
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;
