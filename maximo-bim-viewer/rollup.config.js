import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';

export default {
  //input: 'main.js',
  input: 'IFCJSBIMViewer.js',
  output: {
    file: "build/ifcjs_bundled_iife.js",
    format: 'iife',
    inlineDynamicImports: true, //Necessary for jspdf
    name: "ifcjs"
  },
  plugins: [ resolve(), commonjs() ]
};
