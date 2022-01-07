export const shrinkString = (originStr, maxChars, trailingCharCount) => {
    let shrinkedStr = originStr;
    const shrinkedLength = maxChars - trailingCharCount - 3;
    if (originStr.length > shrinkedLength) {
      const front = originStr.substr(0, shrinkedLength);
      const mid = '...';
      const end = originStr.substr(-trailingCharCount);
      shrinkedStr = front + mid + end;
    }

    return shrinkedStr;
}