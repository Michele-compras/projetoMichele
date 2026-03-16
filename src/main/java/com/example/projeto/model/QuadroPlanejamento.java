package com.example.projeto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "quadro_planejamento", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"colecao", "tipo_solicitacao"})
})
public class QuadroPlanejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String colecao;

    @Column(name = "tipo_solicitacao")
    private String tipoSolicitacao;

    private Integer animeCotado = 0;
    private Integer animeAprovado = 0;
    private Integer animeCancelado = 0;

    private Integer momiCotado = 0;
    private Integer momiAprovado = 0;
    private Integer momiCancelado = 0;

    private Integer authoriaCotado = 0;
    private Integer authoriaAprovado = 0;
    private Integer authoriaCancelado = 0;

    private Integer bimbiCotado = 0;
    private Integer bimbiAprovado = 0;
    private Integer bimbiCancelado = 0;

    private Integer youcciecotado = 0;
    private Integer youccieeAprovado = 0;
    private Integer youcciecancelado = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getColecao() { return colecao; }
    public void setColecao(String colecao) { this.colecao = colecao; }

    public String getTipoSolicitacao() { return tipoSolicitacao; }
    public void setTipoSolicitacao(String tipoSolicitacao) { this.tipoSolicitacao = tipoSolicitacao; }

    public Integer getAnimeCotado() { return animeCotado; }
    public void setAnimeCotado(Integer animeCotado) { this.animeCotado = animeCotado; }

    public Integer getAnimeAprovado() { return animeAprovado; }
    public void setAnimeAprovado(Integer animeAprovado) { this.animeAprovado = animeAprovado; }

    public Integer getAnimeCancelado() { return animeCancelado; }
    public void setAnimeCancelado(Integer animeCancelado) { this.animeCancelado = animeCancelado; }

    public Integer getMomiCotado() { return momiCotado; }
    public void setMomiCotado(Integer momiCotado) { this.momiCotado = momiCotado; }

    public Integer getMomiAprovado() { return momiAprovado; }
    public void setMomiAprovado(Integer momiAprovado) { this.momiAprovado = momiAprovado; }

    public Integer getMomiCancelado() { return momiCancelado; }
    public void setMomiCancelado(Integer momiCancelado) { this.momiCancelado = momiCancelado; }

    public Integer getAuthoriaCotado() { return authoriaCotado; }
    public void setAuthoriaCotado(Integer authoriaCotado) { this.authoriaCotado = authoriaCotado; }

    public Integer getAuthoriaAprovado() { return authoriaAprovado; }
    public void setAuthoriaAprovado(Integer authoriaAprovado) { this.authoriaAprovado = authoriaAprovado; }

    public Integer getAuthoriaCancelado() { return authoriaCancelado; }
    public void setAuthoriaCancelado(Integer authoriaCancelado) { this.authoriaCancelado = authoriaCancelado; }

    public Integer getBimbiCotado() { return bimbiCotado; }
    public void setBimbiCotado(Integer bimbiCotado) { this.bimbiCotado = bimbiCotado; }

    public Integer getBimbiAprovado() { return bimbiAprovado; }
    public void setBimbiAprovado(Integer bimbiAprovado) { this.bimbiAprovado = bimbiAprovado; }

    public Integer getBimbiCancelado() { return bimbiCancelado; }
    public void setBimbiCancelado(Integer bimbiCancelado) { this.bimbiCancelado = bimbiCancelado; }

    public Integer getYoucciecotado() { return youcciecotado; }
    public void setYoucciecotado(Integer youcciecotado) { this.youcciecotado = youcciecotado; }

    public Integer getYouccieeAprovado() { return youccieeAprovado; }
    public void setYouccieeAprovado(Integer youccieeAprovado) { this.youccieeAprovado = youccieeAprovado; }

    public Integer getYoucciecancelado() { return youcciecancelado; }
    public void setYoucciecancelado(Integer youcciecancelado) { this.youcciecancelado = youcciecancelado; }
}
