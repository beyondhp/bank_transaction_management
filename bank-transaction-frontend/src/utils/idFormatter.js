/**
 * Formats a Snowflake algorithm generated ID
 * Makes it more readable, e.g.: 1234567890123456789 => 123-4567-8901-2345-6789
 * 
 * @param {number|string} id The ID to format
 * @returns {string} Formatted ID string
 */
export function formatSnowflakeId(id) {
  if (!id) return '';
  
  // Convert ID to string
  const idStr = String(id);
  
  // If ID length is less than 5, return as is
  if (idStr.length < 5) return idStr;
  
  // Group the ID for display
  const groups = [];
  let remaining = idStr;
  
  // Group every 4 digits from the end
  while (remaining.length > 4) {
    groups.unshift(remaining.slice(-4));
    remaining = remaining.slice(0, -4);
  }
  
  // Add the remaining part to the front
  if (remaining.length > 0) {
    groups.unshift(remaining);
  }
  
  // Join groups with hyphens
  return groups.join('-');
}

/**
 * Copy ID to clipboard
 * 
 * @param {number|string} id The ID to copy
 * @returns {Promise<boolean>} True if successful, false otherwise
 */
export async function copyIdToClipboard(id) {
  if (!id) return false;
  
  try {
    await navigator.clipboard.writeText(String(id));
    return true;
  } catch (err) {
    console.error('Failed to copy ID to clipboard:', err);
    return false;
  }
}

export default {
  formatSnowflakeId,
  copyIdToClipboard
}; 