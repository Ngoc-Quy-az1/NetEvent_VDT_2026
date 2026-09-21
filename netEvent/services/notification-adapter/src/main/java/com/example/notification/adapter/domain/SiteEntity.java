package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "site")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteEntity {

    @Id
    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "station_code", nullable = false, unique = true)
    private String stationCode;

    @Column(name = "area_code", nullable = false)
    private String areaCode;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CellEntity> cells = new ArrayList<>();

    public void addCell(CellEntity cell) {
        cells.add(cell);
        cell.setSite(this);
    }

    public void removeCell(CellEntity cell) {
        cells.remove(cell);
        cell.setSite(null);
    }
}
