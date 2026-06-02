import request from '@/utils/request'

export function listCyanExam(query) {
  return request({
    url: '/exam/exam/list',
    method: 'get',
    params: query
  })
}

export function getCyanExam(examId) {
  return request({
    url: '/exam/exam/' + examId,
    method: 'get'
  })
}

export function addCyanExam(data) {
  return request({
    url: '/exam/exam',
    method: 'post',
    data: data
  })
}

export function updateCyanExam(data) {
  return request({
    url: '/exam/exam',
    method: 'put',
    data: data
  })
}

export function delCyanExam(examIds) {
  return request({
    url: '/exam/exam/' + examIds,
    method: 'delete'
  })
}