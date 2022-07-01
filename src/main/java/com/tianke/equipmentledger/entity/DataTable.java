package com.tianke.equipmentledger.entity;

public class DataTable {
    private int start;
    private int end;
    private int iSortCol;//代表表头0表示第一列
    private String searchValue;
    private String sortRule="DESC";
    private String sEcho;
    private String sortTable;
    private String tableName;
    private String paramValue;
    public DataTable(){
        super();
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getiSortCol() {
        return iSortCol;
    }

    public void setiSortCol(int iSortCol) {
        this.iSortCol = iSortCol;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getSortRule() {
        return sortRule;
    }

    public void setSortRule(String sortRule) {
        this.sortRule = sortRule;
    }

    public String getsEcho() {
        return sEcho;
    }

    public void setsEcho(String sEcho) {
        this.sEcho = sEcho;
    }

    public String getSortTable() {
        return sortTable;
    }

    public void setSortTable(String sortTable) {
        this.sortTable = sortTable;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public String toString() {
        return "DataTable{" +
                "start=" + start +
                ", end=" + end +
                ", iSortCol=" + iSortCol +
                ", searchValue='" + searchValue + '\'' +
                ", sortRule='" + sortRule + '\'' +
                ", sEcho='" + sEcho + '\'' +
                ", sortTable='" + sortTable + '\'' +
                ", tableName='" + tableName + '\'' +
                ", paramValue='" + paramValue + '\'' +
                '}';
    }
}
