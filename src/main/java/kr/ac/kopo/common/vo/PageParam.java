package kr.ac.kopo.common.vo;

public class PageParam {
    private int page = 1;
    private int size = 20;
    private String keyword;
    private String category;
    private String brand;
    private String skinType;
    private String platform;
    private Boolean imageOnly;
    private String sortBy;

    public int getOffset() {
        return (page - 1) * size;
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSkinType() { return skinType; }
    public void setSkinType(String skinType) { this.skinType = skinType; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public Boolean getImageOnly() { return imageOnly; }
    public void setImageOnly(Boolean imageOnly) { this.imageOnly = imageOnly; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    @Override
    public String toString() {
        return "PageParam [page=" + page + ", size=" + size
                + ", keyword=" + keyword + ", category=" + category
                + ", brand=" + brand + ", skinType=" + skinType
                + ", platform=" + platform + ", imageOnly=" + imageOnly
                + ", sortBy=" + sortBy + ", offset=" + getOffset() + "]";
    }
}
