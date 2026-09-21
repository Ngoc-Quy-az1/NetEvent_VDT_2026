package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cell")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CellEntity {

    @Id
    @Column(name = "cell_id")
    private UUID cellId;

    @Column(name = "cell_code", nullable = false, unique = true)
    private String cellCode;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private SiteEntity site;

    @OneToMany(mappedBy = "cell", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CellSessionEntity> cellSessions = new ArrayList<>();

    @Column(name = "device_code", nullable = false)
    private String deviceCode;

    @Column(name = "sector")
    private String sector;

    @Column(name = "network")
    private String network;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "ci_serving_cell")
    private Integer ciServingCell;

    @Column(name = "lac_tac")
    private Integer lacTac;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addCellSession(CellSessionEntity cellSession) {
        cellSessions.add(cellSession);
        cellSession.setCell(this);
    }

    public void removeCellSession(CellSessionEntity cellSession) {
        cellSessions.remove(cellSession);
        cellSession.setCell(null);
    }
}
