<template>
  <div class="transaction-list">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <h3>Transaction Management</h3>
          <el-button type="primary" @click="showCreateDialog" icon="Plus">
            New Transaction
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20" class="mb-4">
        <el-col :span="12">
          <el-select 
            v-model="filterType" 
            placeholder="Filter by Type" 
            clearable
            @change="handleFilterChange"
            style="width: 100%">
            <el-option label="All Types" value="" />
            <el-option label="Deposit" value="DEPOSIT" />
            <el-option label="Withdrawal" value="WITHDRAWAL" />
            <el-option label="Transfer" value="TRANSFER" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-select 
            v-model="filterStatus" 
            placeholder="Filter by Status" 
            clearable
            @change="handleFilterChange"
            style="width: 100%">
            <el-option label="All Status" value="" />
            <el-option label="Initiated" value="INITIATED" />
            <el-option label="Pending" value="PENDING" />
            <el-option label="Processing" value="PROCESSING" />
            <el-option label="Completed" value="COMPLETED" />
            <el-option label="Failed" value="FAILED" />
            <el-option label="Rejected" value="REJECTED" />
            <el-option label="Cancelled" value="CANCELLED" />
          </el-select>
        </el-col>
      </el-row>

      <el-table 
        :data="transactions" 
        style="width: 100%" 
        v-loading="loading"
        stripe
        border
        highlight-current-row
        empty-text="No transactions found"
        :default-sort="{ prop: 'timestamp', order: 'descending' }"
        table-layout="auto"
      >
        <el-table-column prop="id" label="Transaction ID" min-width="220">
          <template #default="scope">
            <div class="id-container">
              <el-tooltip content="Click to copy" placement="top" :show-after="300">
                <span class="id-text" @click="copyToClipboard(scope.row.id)">{{ formatId(scope.row.id) }}</span>
              </el-tooltip>
              <el-button 
                size="small" 
                circle 
                class="copy-button"
                @click="copyToClipboard(scope.row.id)" 
                title="Copy ID"
              >
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="Description" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sourceAccount" label="From Account" min-width="140" show-overflow-tooltip />
        <el-table-column prop="destinationAccount" label="To Account" min-width="140" show-overflow-tooltip />
        <el-table-column prop="amount" label="Amount" min-width="110" sortable>
          <template #default="scope">
            <span :class="getAmountClass(scope.row.type)">{{ formatAmount(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="Type" min-width="100" sortable>
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" effect="plain">
              {{ translateType(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="Status" min-width="110" sortable>
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timestamp" label="Date" min-width="120" sortable>
          <template #default="scope">
            {{ formatDate(scope.row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" min-width="130" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain @click="showEditDialog(scope.row)">
                <el-icon><EditPen /></el-icon> Edit
              </el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">
                <el-icon><Delete /></el-icon> Delete
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
      destroy-on-close
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="transactionForm" 
        label-width="140px"
        status-icon
        @submit.prevent="handleSubmit"
        label-position="left"
      >
        <el-form-item label="Description" prop="description">
          <el-input 
            v-model="form.description" 
            placeholder="Enter transaction description"
            :maxlength="255"
            show-word-limit
            style="width: 90%"
          />
        </el-form-item>
        
        <el-form-item label="Type" prop="type">
          <el-select 
            v-model="form.type" 
            style="width: 50%" 
            placeholder="Select transaction type"
            @change="handleTypeChange"
          >
            <el-option label="Deposit" value="DEPOSIT" />
            <el-option label="Withdrawal" value="WITHDRAWAL" />
            <el-option label="Transfer" value="TRANSFER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Amount" prop="amount">
          <el-input
            v-model.number="form.amount" 
            type="number"
            style="width: 40%"
            placeholder="Enter amount"
            min="0.01"
            step="0.01"
            @blur="validateAmount"
          >
            <template #prefix>$</template>
          </el-input>
          <div class="form-tip">Amount must be greater than 0</div>
        </el-form-item>
        
        <el-form-item label="Source Account" prop="sourceAccount" :required="isSourceAccountRequired()">
          <el-input 
            v-model="form.sourceAccount" 
            placeholder="e.g., ACCT12345678"
            :maxlength="20"
            :disabled="isSourceAccountDisabled()"
            :class="{ 'disabled-input': isSourceAccountDisabled() }"
            style="width: 60%"
          />
        </el-form-item>
        
        <el-form-item label="Destination Account" prop="destinationAccount" :required="isDestinationAccountRequired()">
          <el-input 
            v-model="form.destinationAccount" 
            placeholder="e.g., ACCT87654321"
            :maxlength="20"
            :disabled="isDestinationAccountDisabled()"
            :class="{ 'disabled-input': isDestinationAccountDisabled() }"
            style="width: 60%"
          />
        </el-form-item>
        
        <el-form-item label="Status" prop="status">
          <el-select v-model="form.status" style="width: 70%" placeholder="Select status">
            <el-option label="Initiated" value="INITIATED" />
            <el-option label="Pending" value="PENDING" />
            <el-option label="Processing" value="PROCESSING" />
            <el-option label="Completed" value="COMPLETED" />
            <el-option label="Failed" value="FAILED" />
            <el-option label="Rejected" value="REJECTED" />
            <el-option label="Cancelled" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitForm">
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, CopyDocument } from '@element-plus/icons-vue'
import { formatSnowflakeId, copyIdToClipboard } from '@/utils/idFormatter'

/**
 * Transaction List Component
 * Handles the display and management of bank transactions
 */
export default {
  name: 'TransactionList',
  components: {
    EditPen,
    Delete,
    CopyDocument
  },
  
  data() {
    // Custom validator for account format
    const validateAccountFormat = (rule, value, callback) => {
      // Skip validation for source account when transaction type is DEPOSIT
      if (rule.field === 'sourceAccount' && this.form.type === 'DEPOSIT') {
        return callback(); // Always pass validation
      }
      
      // Skip validation for destination account when transaction type is WITHDRAWAL
      if (rule.field === 'destinationAccount' && this.form.type === 'WITHDRAWAL') {
        return callback(); // Always pass validation
      }
      
      // For required fields, check if value is provided when required
      if ((rule.field === 'sourceAccount' && this.isSourceAccountRequired()) || 
          (rule.field === 'destinationAccount' && this.isDestinationAccountRequired())) {
        if (!value || value.trim() === '') {
          return callback(new Error(rule.field === 'sourceAccount' ? 
                               'Source account is required' : 
                               'Destination account is required'));
        }
      }
      
      // For fields with values, validate format
      if (value && value.trim() !== '') {
        const accountPattern = /^[A-Z0-9]{8,20}$/;
        if (!accountPattern.test(value)) {
          callback(new Error('Account must be 8-20 uppercase alphanumeric characters'));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };
    
    return {
      transactions: [],
      loading: false,
      currentPage: 1,
      pageSize: 20,
      totalItems: 0,
      dialogVisible: false,
      dialogTitle: 'Create Transaction',
      editingId: null,
      filterType: '',
      filterStatus: '',
      form: {
        description: '',
        amount: null, // Set to null instead of 0 to trigger validation
        type: 'TRANSFER', // Default to transfer
        sourceAccount: '',
        destinationAccount: '',
        status: 'INITIATED'
      },
      rules: {
        description: [
          { required: true, message: 'Please enter description', trigger: 'blur' },
          { min: 3, max: 255, message: 'Length should be 3 to 255 characters', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: 'Please enter amount', trigger: 'blur' },
          { 
            validator: (rule, value, callback) => {
              // Skip validation if no value (other rule will catch this)
              if (!value && value !== 0) {
                callback();
                return;
              }
              
              // Convert to number and validate
              const numValue = Number(value);
              if (isNaN(numValue) || numValue <= 0) {
                callback(new Error('Amount must be greater than 0'));
              } else {
                callback();
              }
            }, 
            trigger: 'blur' 
          }
        ],
        type: [
          { required: true, message: 'Please select transaction type', trigger: 'change' }
        ],
        sourceAccount: [
          { 
            required: true, 
            message: 'Please enter source account', 
            trigger: 'blur',
            validator: validateAccountFormat
          },
        ],
        destinationAccount: [
          { 
            required: true, 
            message: 'Please enter destination account', 
            trigger: 'blur',
            validator: validateAccountFormat
          }
        ],
        status: [
          { required: true, message: 'Please select status', trigger: 'change' }
        ]
      }
    }
  },
  
  created() {
    this.fetchTransactions()
  },
  
  methods: {
    /**
     * Handles the transaction type change
     * Revalidates the form and updates UI
     */
    handleTypeChange() {
      // Clear account fields if not applicable for the new type
      if (this.form.type === 'DEPOSIT') {
        // For DEPOSIT, source account should be null (not empty string)
        this.form.sourceAccount = '';
        
        // Ensure the source account input is disabled
        this.$nextTick(() => {
          const sourceAccountInput = this.$refs.transactionForm.fields.find(f => f.prop === 'sourceAccount');
          if (sourceAccountInput) {
            sourceAccountInput.resetField(); // Clear any validation errors
          }
        });
      } else if (this.form.type === 'WITHDRAWAL') {
        // For WITHDRAWAL, destination account should be null
        this.form.destinationAccount = '';
        
        // Ensure the destination account input is disabled
        this.$nextTick(() => {
          const destAccountInput = this.$refs.transactionForm.fields.find(f => f.prop === 'destinationAccount');
          if (destAccountInput) {
            destAccountInput.resetField(); // Clear any validation errors
          }
        });
      }
      
      // Revalidate the form with proper error handling
      if (this.$refs.transactionForm) {
        this.$refs.transactionForm.validateField(['sourceAccount', 'destinationAccount'], (valid, invalidFields) => {
          // Validation callback - errors will be shown on the form automatically
          if (!valid) {
            console.log('Field validation errors:', invalidFields);
          }
        });
      }
    },
    
    /**
     * Checks if source account is required based on transaction type
     */
    isSourceAccountRequired() {
      return this.form.type === 'WITHDRAWAL' || this.form.type === 'TRANSFER';
    },
    
    /**
     * Checks if destination account is required based on transaction type
     */
    isDestinationAccountRequired() {
      return this.form.type === 'DEPOSIT' || this.form.type === 'TRANSFER';
    },
    
    /**
     * Checks if source account should be disabled based on transaction type
     */
    isSourceAccountDisabled() {
      return this.form.type === 'DEPOSIT';
    },
    
    /**
     * Checks if destination account should be disabled based on transaction type
     */
    isDestinationAccountDisabled() {
      return this.form.type === 'WITHDRAWAL';
    },

    /**
     * Returns CSS class based on transaction type
     * @param {string} type - Transaction type
     * @returns {string} CSS class
     */
    getAmountClass(type) {
      switch(type) {
        case 'DEPOSIT':
          return 'amount-positive';
        case 'WITHDRAWAL':
          return 'amount-negative';
        default:
          return '';
      }
    },

    /**
     * Returns tag type based on transaction type
     * @param {string} type - Transaction type
     * @returns {string} Tag type
     */
    getTypeTag(type) {
      switch(type) {
        case 'DEPOSIT':
          return 'success';
        case 'WITHDRAWAL':
          return 'danger';
        case 'TRANSFER':
          return 'info';
        default:
          return '';
      }
    },

    /**
     * Handles filter changes
     */
    handleFilterChange() {
      this.currentPage = 1; // Reset to first page
      this.fetchTransactions();
    },

    /**
     * Fetches transactions from the API with filters
     */
    async fetchTransactions() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        }
        
        // Add filters if they are set
        if (this.filterType) {
          params.type = this.filterType;
        }
        
        if (this.filterStatus) {
          params.status = this.filterStatus;
        }
        
        const response = await axios.get('/api/transactions/paged', { params })
        this.transactions = response.data.content
        this.totalItems = response.data.totalElements
      } catch (error) {
        console.error('Failed to fetch transactions:', error)
        ElMessage.error('Failed to load transactions')
      } finally {
        this.loading = false
      }
    },

    /**
     * Handle page size change
     * @param {number} newSize - New page size
     */
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.currentPage = 1 // Reset to first page when changing page size
      this.fetchTransactions()
    },

    /**
     * Handle current page change
     * @param {number} newPage - New current page
     */
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.fetchTransactions()
    },

    /**
     * Formats the amount with currency symbol
     * @param {number} amount - The amount to format
     * @returns {string} Formatted amount with currency symbol
     */
    formatAmount(amount) {
      const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2
      });
      return formatter.format(amount);
    },

    /**
     * Formats the date to locale string
     * @param {string} date - The date to format
     * @returns {string} Formatted date string
     */
    formatDate(date) {
      return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    },

    /**
     * Translates transaction type to display text
     * @param {string} type - The transaction type
     * @returns {string} Human readable transaction type
     */
    translateType(type) {
      const types = {
        DEPOSIT: 'Deposit',
        WITHDRAWAL: 'Withdrawal',
        TRANSFER: 'Transfer'
      }
      return types[type] || type
    },
    
    /**
     * Formats the transaction status for display
     * @param {string} status - The transaction status
     * @returns {string} Human readable status
     */
    formatStatus(status) {
      if (!status) return '';
      // Convert PENDING to Pending, COMPLETED to Completed, etc.
      return status.charAt(0) + status.slice(1).toLowerCase().replace(/_/g, ' ');
    },
    
    /**
     * Gets the appropriate Element Plus tag type based on status
     * @param {string} status - The transaction status
     * @returns {string} Tag type for styling
     */
    getStatusType(status) {
      const statusMap = {
        INITIATED: 'info',
        PENDING: 'warning',
        PROCESSING: 'info',
        COMPLETED: 'success',
        FAILED: 'danger',
        REJECTED: 'danger',
        CANCELLED: 'info'
      };
      return statusMap[status] || 'info';
    },

    /**
     * Shows the dialog for creating a new transaction
     */
    showCreateDialog() {
      this.dialogTitle = 'New Transaction'
      this.form = {
        description: '',
        amount: null, // Set to null instead of 0
        type: 'DEPOSIT',
        sourceAccount: '',
        destinationAccount: '',
        status: 'INITIATED'
      }
      this.editingId = null
      this.dialogVisible = true
      
      // Reset form validation
      if (this.$refs.transactionForm) {
        this.$refs.transactionForm.resetFields()
      }
    },

    /**
     * Shows the dialog for editing an existing transaction
     * @param {Object} transaction - The transaction to edit
     */
    showEditDialog(transaction) {
      this.dialogTitle = 'Edit Transaction'
      this.form = {
        description: transaction.description,
        amount: transaction.amount,
        type: transaction.type,
        sourceAccount: transaction.sourceAccount || '',
        destinationAccount: transaction.destinationAccount || '',
        status: transaction.status || 'PENDING'
      }
      this.editingId = transaction.id
      this.dialogVisible = true
      
      // Reset form validation
      if (this.$refs.transactionForm) {
        this.$refs.transactionForm.resetFields()
      }
    },

    /**
     * Validates and submits the form
     */
    submitForm() {
      this.$refs.transactionForm.validate(async (valid) => {
        if (valid) {
          await this.handleSubmit()
        } else {
          ElMessage.warning('Please correct the errors in the form')
          return false
        }
      })
    },

    /**
     * Handles the form submission for both create and edit operations
     */
    async handleSubmit() {
      try {
        // Prepare form data - ensure account fields are correctly set based on transaction type
        const formData = {...this.form};
        
        // Ensure amount is a valid number
        // Handle special cases like '-', empty string, etc.
        const rawAmount = formData.amount;
        if (rawAmount === '-' || rawAmount === null || rawAmount === '') {
          ElMessage.error('Please enter a valid amount');
          this.form.amount = null;
          this.$refs.transactionForm.validateField('amount');
          return;
        }
        
        formData.amount = Number(formData.amount);
        if (isNaN(formData.amount) || formData.amount <= 0) {
          ElMessage.error('Amount must be a valid positive number');
          // If negative or invalid, reset to null and trigger validation
          this.form.amount = null;
          this.$refs.transactionForm.validateField('amount');
          return;
        }
        
        // For DEPOSIT, clear source account (set to null instead of empty string)
        if (formData.type === 'DEPOSIT') {
          formData.sourceAccount = null;
        }
        
        // For WITHDRAWAL, clear destination account
        if (formData.type === 'WITHDRAWAL') {
          formData.destinationAccount = null;
        }

        if (this.editingId) {
          console.log('Updating transaction with data:', formData);
          await axios.put(`/api/transactions/${this.editingId}`, formData)
          ElMessage.success('Transaction updated successfully')
        } else {
          console.log('Creating transaction with data:', formData);
          const response = await axios.post('/api/transactions', formData)
          console.log('Server response:', response.data);
          ElMessage.success('Transaction created successfully')
        }
        this.dialogVisible = false
        // Refresh the current page to show updated data
        await this.fetchTransactions()
      } catch (error) {
        console.error('Operation failed:', error)
        if (error.response) {
          console.error('Error response:', error.response.data);
          
          // Handle validation errors from the server
          const errorData = error.response.data;
          if (error.response.status === 400 && typeof errorData === 'object') {
            const errorMessages = [];
            for (const field in errorData) {
              errorMessages.push(`${field}: ${errorData[field]}`);
            }
            
            if (errorMessages.length > 0) {
              ElMessage.error(`Validation errors: ${errorMessages.join(', ')}`);
              return;
            }
          }
        }
        ElMessage.error(`Operation failed: ${error.response?.data?.error || error.message || 'Unknown error'}`)
      }
    },

    /**
     * Handles the deletion of a transaction
     * @param {Object} transaction - The transaction to delete
     */
    async handleDelete(transaction) {
      try {
        await ElMessageBox.confirm('Are you sure you want to delete this transaction?', 'Warning', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        })
        await axios.delete(`/api/transactions/${transaction.id}`)
        ElMessage.success('Deleted successfully')
        await this.fetchTransactions()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Delete failed:', error)
          ElMessage.error('Delete failed')
        }
      }
    },

    /**
     * Format the Snowflake algorithm generated ID
     * @param {number} id Transaction ID
     * @returns {string} Formatted ID
     */
    formatId(id) {
      return formatSnowflakeId(id);
    },

    /**
     * Copy the transaction ID to clipboard
     * @param {number} id - The transaction ID to copy
     */
    copyToClipboard(id) {
      copyIdToClipboard(id)
        .then(success => {
          if (success) {
            ElMessage({
              message: 'Transaction ID copied to clipboard',
              type: 'success',
              duration: 1500
            });
          } else {
            ElMessage.error('Failed to copy ID');
          }
        });
    },

    /**
     * Validates the amount field on input
     */
    validateAmount(event) {
      // Handle the event more carefully
      try {
        // Check if the event is a string (which might be '-' or other non-numeric input)
        const value = Number(event);
        
        // Only validate and reset if it's actually a negative number
        // This avoids errors when the user is in the middle of typing (e.g., just typed '-')
        if (!isNaN(value) && value <= 0) {
          this.form.amount = null;
          this.$refs.transactionForm.validateField('amount');
          ElMessage.warning('Amount must be a positive number');
        }
      } catch (error) {
        console.error('Error in amount validation:', error);
        // Don't show an error message here to avoid disrupting the user experience
      }
    }
  }
}
</script>

<style scoped>
/* Container styling */
.transaction-list {
  max-width: 1600px;
  margin: 0 auto;
}

/* Card header styling */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

/* Margin utility class */
.mb-4 {
  margin-bottom: 20px;
}

/* Pagination container */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Form styling */
.el-form-item-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
  padding-top: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-form-item__error) {
  position: static;
  margin-top: 2px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item.is-required > .el-form-item__label::before) {
  content: '*';
  color: #F56C6C;
  margin-right: 4px;
}

/* Disabled input styling */
.disabled-input {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #C0C4CC;
}

/* Amount styling */
.amount-positive {
  color: #67C23A;
  font-weight: bold;
}

.amount-negative {
  color: #F56C6C;
  font-weight: bold;
}

/* Responsive table adjustments */
@media (max-width: 768px) {
  .el-button-group {
    display: flex;
    flex-direction: column;
  }
  
  .el-button-group .el-button {
    margin-bottom: 5px;
  }
}

/* ID container styles */
.id-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.id-text {
  cursor: pointer;
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
  font-family: monospace;
  font-size: 12px;
}

.id-text:hover {
  text-decoration: underline;
  color: #409EFF;
}

.copy-button {
  padding: 2px;
  height: 20px;
  width: 20px;
  flex-shrink: 0;
}

/* Make table content adaptive */
:deep(.el-table) {
  width: 100% !important;
  table-layout: auto;
}

:deep(.el-table .cell) {
  word-break: normal;
  white-space: nowrap;
}

/* Optimize display on smaller screens */
@media (max-width: 992px) {
  :deep(.el-table .cell) {
    white-space: normal;
  }
}
</style> 