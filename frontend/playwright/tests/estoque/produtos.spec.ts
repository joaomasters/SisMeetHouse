import { test, expect } from '@playwright/test';
import sidebarLocators from '../../locators/sidebar.locators'
import produtosLocators from '../../locators/estoque/produtos.locators'
import { faker } from '@faker-js/faker';

test.describe('Cadastro de produtos', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('Não deve permitir cadastrar um produto com espaços em branco em seu nome', async ({ page }) => {

    await page.locator(sidebarLocators.produtos).click();
    await page.locator(produtosLocators.btnNovoProduto).click();
    await page.locator(produtosLocators.novoProduto.nome).fill(' ');
    await page.locator(produtosLocators.novoProduto.precoVenda).fill(faker.commerce.price());
    await page.locator(produtosLocators.novoProduto.salvar).click();
    
    //toast
    await expect(page.getByText('Nome não pode ser vazio', { exact: true })).toBeVisible();
  });

  test('Deve permitir o cadastro de produto preenchendo todos os campos com sucesso', async ({ page }) => {
    await page.locator(sidebarLocators.produtos).click();
    await page.locator(produtosLocators.btnNovoProduto).click();
    await page.locator(produtosLocators.novoProduto.nome).fill(faker.commerce.productName());
    await page.locator(produtosLocators.novoProduto.unidade).selectOption('CX')
    await page.locator(produtosLocators.novoProduto.tipo).selectOption('SUBPRODUTO')
    await page.locator(produtosLocators.novoProduto.pluBalanca).fill('123456')
    await page.locator(produtosLocators.novoProduto.precoVenda).fill(faker.commerce.price());
    await page.locator(produtosLocators.novoProduto.precoCusto).fill(faker.commerce.price());
    await page.locator(produtosLocators.novoProduto.estoqueMinimo).fill('18');
    await page.locator(produtosLocators.novoProduto.ean13).fill(faker.string.numeric(13));
    await page.locator(produtosLocators.novoProduto.salvar).click();
    
    //toast
    await expect(page.getByText('Produto criado!', { exact: true })).toBeVisible();
  })
})
  