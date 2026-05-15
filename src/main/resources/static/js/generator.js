document.addEventListener('alpine:init', () => {
  Alpine.data('generator', () => ({
    searchTableName: '',
    tableData: [],
    selectedTables: [],
    selectAll: false,
    currentPage: 1,
    pageSize: 10,
    totalCount: 0,
    totalPage: 0,

    settings: {
      package: '',
      moduleName: '',
      tablePrefix: ''
    },

    options: {
      generateType: 'boot',
      helioFrameworkVersion: 'v2',
      queryFormSchema: true,
      serviceAndImpl: false,
      mybatisXML: false,
      useYesOrNoEnum: true,
      useEnabledStatusEnum: true
    },

    toast: { show: false, message: '', type: 'success' },

    init() {
      this.loadData();
      this.loadSettings();
    },

    get pageNumbers() {
      const pages = [];
      const tp = this.totalPage;
      const cp = this.currentPage;
      if (tp <= 7) {
        for (let i = 1; i <= tp; i++) pages.push(i);
      } else {
        pages.push(1);
        if (cp > 3) pages.push('...');
        for (let i = Math.max(2, cp - 1); i <= Math.min(tp - 1, cp + 1); i++) pages.push(i);
        if (cp < tp - 2) pages.push('...');
        pages.push(tp);
      }
      return pages;
    },

    async loadData() {
      const params = new URLSearchParams({
        page: this.currentPage,
        limit: this.pageSize
      });
      if (this.searchTableName) {
        params.set('tableName', this.searchTableName);
      }
      try {
        const res = await fetch('sys/generator/list?' + params.toString());
        const data = await res.json();
        if (data.page) {
          this.tableData = data.page.list || [];
          this.currentPage = data.page.currPage || 1;
          this.totalPage = data.page.totalPage || 0;
          this.totalCount = data.page.totalCount || 0;
        }
        this.selectedTables = [];
        this.selectAll = false;
      } catch (e) {
        this.showToast('加载数据失败', 'error');
      }
    },

    query() {
      this.currentPage = 1;
      this.loadData();
    },

    goToPage(page) {
      if (page === '...') return;
      this.currentPage = page;
      this.loadData();
    },

    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedTables = this.tableData.map(r => r.tableName);
      } else {
        this.selectedTables = [];
      }
    },

    generate() {
      if (this.selectedTables.length === 0) {
        this.showToast('请至少选择一张表', 'error');
        return;
      }
      const params = new URLSearchParams({
        tables: this.selectedTables.join(','),
        generateType: this.options.generateType,
        helioFrameworkVersion: this.options.helioFrameworkVersion,
        queryFormSchema: !!this.options.queryFormSchema,
        serviceAndImpl: !!this.options.serviceAndImpl,
        mybatisXML: !!this.options.mybatisXML,
        useYesOrNoEnum: !!this.options.useYesOrNoEnum,
        useEnabledStatusEnum: !!this.options.useEnabledStatusEnum
      });
      location.href = 'sys/generator/code?' + params.toString();
    },

    async loadSettings() {
      try {
        const res = await fetch('sys/generator/settings');
        const data = await res.json();
        if (data.code === 0 && data.settings) {
          this.settings.package = data.settings.package || '';
          this.settings.moduleName = data.settings.moduleName || '';
          this.settings.tablePrefix = data.settings.tablePrefix || '';
        }
      } catch (e) {
        // settings load failure is non-critical
      }
    },

    async saveSettings() {
      try {
        const res = await fetch('sys/generator/settings/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.settings)
        });
        const data = await res.json();
        if (data.code === 0) {
          this.showToast('设置已保存');
        } else {
          this.showToast('保存失败：' + (data.msg || '未知错误'), 'error');
        }
      } catch (e) {
        this.showToast('保存失败：网络错误', 'error');
      }
    },

    showToast(message, type = 'success') {
      this.toast = { show: true, message, type };
      setTimeout(() => { this.toast.show = false; }, 2500);
    }
  }));
});
