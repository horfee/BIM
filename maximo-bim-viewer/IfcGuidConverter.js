"use strict";
// Source : https://github.com/Zhuangkh/IDConverter/blob/master/IDConverter/IDConverter/IfcGuidConverter.cs

const base64Chars = [
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
    'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
    'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
    'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '_', '$'
];

function CvFrom64(str, start, len) {

    var res = 0;
    var i;
    for( i = 0; i < len; i++ ) {
        var index = -1;
        var j;
        for( j = 0; j < base64Chars.length; j++ ) {
            if ( base64Chars[j] === str[start+i] ) {
                index = j;
                break;
            }
        }
        res = res * 64 + index;
    }
    return res;
}

function CvTo64( number, result, start, len ) {
    var act = number;
    var digits = len;
    var digit;
    for ( digit = 0; digit < digits; digit++ ) {
        result[start + len - digit - 1] = base64Chars[(act % 64)];
        act /= 64;
        act = parseInt(act);
    }

}

function BitConverter(arr, startIndex, width) {
    var res = 0;
    var i;
    for(i =  startIndex + width - 1; i >= startIndex; i--) {
        res = res << 8;
        if ( res < 0 ) res = res >>> 0;
        res |= arr[i];
        if ( res < 0 ) res = res >>> 0;
    }
    return res >= 0 ? res : res >>> 0;
}

function guidToByteArray(revitGuid) {
    var text = revitGuid.replaceAll("-","");
    var i = 0;
    var res = [];
    while ( i < text.length ) {
        res.push(parseInt(text.substr(i,2), 16));
        i += 2;
    }

    [res[0], res[1], res[2], res[3], res[4], res[5], res[6], res[7]] = [res[3], res[2], res[1], res[0], res[5], res[4], res[7], res[6]];

    return new Uint32Array(res);
}
export function toIfcGuid(revitGuid) {
    var num = [];
    var str = [];

    const b = guidToByteArray(revitGuid);

    num[0] = parseInt(BitConverter(b, 0, 4) / 16777216);
    num[1] = BitConverter(b, 0, 4) % 16777216;
    num[2] = (BitConverter(b, 4, 2) * 256 + parseInt(BitConverter(b, 6, 2) / 256));
    num[3] = (BitConverter(b, 6, 2) % 256 * 65536 + b[8] * 256 + b[9]);
    num[4] = (b[10] * 65536 + b[11] * 256 + b[12]);
    num[5] = (b[13] * 65536 + b[14] * 256 + b[15]);

    var n = 2;
    var pos = 0;
    var i;
    for( i of num) {
        CvTo64(i, str, pos, n);
        pos += n;
        n = 4;
    }
    return str.join("");
}

export function fromIfcGuid(guid) {
    const num = new Uint32Array(6);
    var n = 2;
    var pos = 0;
    var i;
    for( i = 0; i < num.length; i++ ) {
        num[i] = CvFrom64(guid, pos, n);
        pos += n;
        n = 4;
    }

    const a = parseInt(num[0] * 16777216 + num[1]);
    const b = parseInt(num[2] / 256);
    const c = parseInt(num[2] % 256 * 256 + num[3] / 65536);
    const d = [
        (parseInt(num[3] / 256) % 256),
        num[3] % 256,
        parseInt(num[4] / 65536),
        (parseInt(num[4] / 256) % 256),
        num[4] % 256,
        parseInt(num[5] / 65536),
        (parseInt(num[5] / 256) % 256),
        num[5] % 256
    ];
    return  a.toString(16) + "-" + 
            b.toString(16) + "-" + 
            c.toString(16)+ "-" + 
            d[0].toString(16) + d[1].toString(16) + "-" + 
            d[2].toString(16) + d[3].toString(16) + d[4].toString(16) + d[5].toString(16) + d[6].toString(16) + d[7].toString(16);

}

